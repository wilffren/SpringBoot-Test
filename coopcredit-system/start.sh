#!/bin/bash

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║           🚀 STARTING COOPCREDIT SYSTEM                        ║"
echo "╚════════════════════════════════════════════════════════════════╝"

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Kill existing processes on ports
echo "🔄 Liberando puertos..."
lsof -ti :8080 -ti :8081 2>/dev/null | xargs -r kill -9 2>/dev/null
sleep 2

# Start Docker containers
echo "🐳 Iniciando contenedores Docker..."
docker start coopcredit-mysql coopcredit-prometheus coopcredit-grafana 2>/dev/null || {
    echo "   Creando contenedores..."
    docker run -d --name coopcredit-mysql --network host -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=coopcredit mysql:8.0 --port=3307 2>/dev/null
    docker run -d --name coopcredit-prometheus --network host -v "$SCRIPT_DIR/prometheus.yml:/etc/prometheus/prometheus.yml" prom/prometheus:latest --config.file=/etc/prometheus/prometheus.yml --web.listen-address=:9091 2>/dev/null
    docker run -d --name coopcredit-grafana --network host -e GF_SECURITY_ADMIN_USER=admin -e GF_SECURITY_ADMIN_PASSWORD=admin grafana/grafana:latest 2>/dev/null
}

echo "⏳ Esperando MySQL (5 segundos)..."
sleep 5

# Start Risk Central Mock Service in background
echo "🔧 Iniciando Risk Central Mock Service..."
cd "$SCRIPT_DIR/risk-central-mock-service"
mvn spring-boot:run -DskipTests > /dev/null 2>&1 &
RISK_PID=$!

sleep 8

# Start Credit Application Service in foreground
echo "💳 Iniciando Credit Application Service..."
cd "$SCRIPT_DIR/credit-application-service"
mvn spring-boot:run -DskipTests

# Cleanup on exit
kill $RISK_PID 2>/dev/null
