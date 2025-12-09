package com.coopcredit.creditapplication.infrastructure.config;

import com.coopcredit.creditapplication.domain.model.User;
import com.coopcredit.creditapplication.domain.model.enums.UserRole;
import com.coopcredit.creditapplication.domain.ports.out.UserRepositoryPort;
import com.coopcredit.creditapplication.infrastructure.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Configuration
public class StartupConfig {

    private static final Logger log = LoggerFactory.getLogger(StartupConfig.class);
    
    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    private String testToken;
    
    public StartupConfig(UserRepositoryPort userRepository, 
                         PasswordEncoder passwordEncoder,
                         JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    @Order(1)
    public CommandLineRunner startDockerServices() {
        return args -> {
            log.info("Starting Docker services...");
            
            // Start MySQL
            startContainer("coopcredit-mysql", 
                "docker run -d --name coopcredit-mysql --network host " +
                "-e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=coopcredit " +
                "mysql:8.0 --port=3307");
            
            // Start Prometheus
            String basePath = System.getProperty("user.dir").replace("/credit-application-service", "");
            String prometheusPath = basePath + "/prometheus.yml";
            startContainer("coopcredit-prometheus",
                "docker run -d --name coopcredit-prometheus --network host " +
                "-v " + prometheusPath + ":/etc/prometheus/prometheus.yml " +
                "prom/prometheus:latest --config.file=/etc/prometheus/prometheus.yml --web.listen-address=:9091");
            
            // Start Grafana
            startContainer("coopcredit-grafana",
                "docker run -d --name coopcredit-grafana --network host " +
                "-e GF_SECURITY_ADMIN_USER=admin -e GF_SECURITY_ADMIN_PASSWORD=admin " +
                "grafana/grafana:latest");
            
            // Wait for MySQL to be ready
            log.info("Waiting for MySQL to be ready...");
            Thread.sleep(3000);
            
            // Create test user and generate token
            createTestUserAndToken();
        };
    }
    
    private void createTestUserAndToken() {
        try {
            if (!userRepository.existsByUsername("admin")) {
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .role(UserRole.ROLE_ADMIN)
                        .enabled(true)
                        .build();
                user = userRepository.save(user);
                testToken = jwtTokenProvider.generateToken(user);
                log.info("Test user created: admin/admin123");
            } else {
                User user = userRepository.findByUsername("admin").orElse(null);
                if (user != null) {
                    testToken = jwtTokenProvider.generateToken(user);
                }
            }
        } catch (Exception e) {
            log.warn("Could not create test user: {}", e.getMessage());
        }
    }

    private void startContainer(String name, String command) {
        try {
            // Check if container exists
            Process checkProcess = Runtime.getRuntime().exec(new String[]{"docker", "ps", "-a", "-q", "-f", "name=" + name});
            BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()));
            String containerId = reader.readLine();
            checkProcess.waitFor();
            
            if (containerId != null && !containerId.isEmpty()) {
                // Container exists, try to start it
                log.info("Container {} exists, starting...", name);
                Process startProcess = Runtime.getRuntime().exec(new String[]{"docker", "start", name});
                startProcess.waitFor();
            } else {
                // Container doesn't exist, create it
                log.info("Creating container {}...", name);
                Process createProcess = Runtime.getRuntime().exec(command.split(" "));
                createProcess.waitFor();
            }
        } catch (Exception e) {
            log.warn("Could not start container {}: {}", name, e.getMessage());
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void displayEndpoints() {
        String banner = """
            
            ╔══════════════════════════════════════════════════════════════════════════════════════╗
            ║                        🚀 COOPCREDIT SYSTEM - READY                                 ║
            ╠══════════════════════════════════════════════════════════════════════════════════════╣
            ║                                                                                      ║
            ║  📚 SWAGGER - CREDIT SERVICE:    http://localhost:8080/swagger-ui.html               ║
            ║  📋 SWAGGER - RISK CENTRAL:      http://localhost:8081/swagger-ui.html               ║
            ║  📊 GRAFANA:                     http://localhost:3000  (admin/admin)                ║
            ║  📈 PROMETHEUS:                  http://localhost:9091                               ║
            ║                                                                                      ║
            ╠══════════════════════════════════════════════════════════════════════════════════════╣
            ║  🔐 AUTH (No JWT required):                                                          ║
            ║     POST /api/auth/register  - Register new user                                     ║
            ║     POST /api/auth/login     - Get JWT token                                         ║
            ║                                                                                      ║
            ║  🔒 PROTECTED ENDPOINTS (Require JWT in header: Authorization: Bearer <token>)       ║
            ║     /api/members/**             - Member management                                  ║
            ║     /api/credit-applications/** - Credit applications                                ║
            ║                                                                                      ║
            ╠══════════════════════════════════════════════════════════════════════════════════════╣
            ║  🔑 TEST CREDENTIALS:  admin / admin123                                              ║
            ╚══════════════════════════════════════════════════════════════════════════════════════╝
            """;
        
        if (testToken != null) {
            banner += "\n  📋 TEST JWT TOKEN (copy this to Swagger Authorize):\n  Bearer " + testToken + "\n";
        }
        
        System.out.println(banner);
    }
}
