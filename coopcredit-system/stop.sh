#!/bin/bash

echo "========================================"
echo "   STOPPING COOPCREDIT SYSTEM          "
echo "========================================"

# Stop Java processes
echo "Stopping Spring Boot services..."
pkill -f "spring-boot:run" 2>/dev/null
pkill -f "credit-application-service" 2>/dev/null
pkill -f "risk-central-mock-service" 2>/dev/null

# Kill processes on ports
echo "Releasing ports 8080 and 8081..."
lsof -ti :8080 -ti :8081 2>/dev/null | xargs -r kill -9 2>/dev/null

# Stop Docker containers
echo "Stopping Docker containers..."
docker stop coopcredit-mysql coopcredit-prometheus coopcredit-grafana 2>/dev/null

echo ""
echo "========================================"
echo "   COOPCREDIT SYSTEM STOPPED           "
echo "========================================"
echo ""
echo "To start again, run: ./start.sh"
