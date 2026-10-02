package ru.valkeru.libdemo.test;

import com.redis.testcontainers.RedisContainer;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;

/**
 * Configuration to start application to be tested
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestingApplicationConfiguration {

    private static final Network dockerNetwork = Network.newNetwork();

    private static GenericContainer<?> applicationContainer;
    private static PostgreSQLContainer<?> postgreSQLContainer;
    private static RedisContainer redisContainer;

    @Bean
    public GenericContainer<?> applicationContainer(@Value("${configuration.api.path}") String apiPath,
                                                    @Value("${configuration.application-tag}") String applicationTag,
                                                    PostgreSQLContainer<?> postgreSQLContainer,
                                                    RedisContainer redisContainer) {
        if (applicationContainer == null) {
            applicationContainer = new GenericContainer<>(
                DockerImageName.parse("ghcr.io/valkeru/libdemo:%s".formatted(applicationTag))
            )
                .withNetwork(dockerNetwork)
                .dependsOn(postgreSQLContainer, redisContainer)
                .withEnv(
                    Map.ofEntries(
                        Map.entry("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/libdemo?reWriteBatchedInserts=true"),
                        Map.entry("SPRING_DATASOURCE_USERNAME", postgreSQLContainer.getUsername()),
                        Map.entry("SPRING_DATASOURCE_PASSWORD", postgreSQLContainer.getPassword()),
                        Map.entry("SPRING_PROFILES_ACTIVE", "production"),
                        Map.entry("MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE", "health"),
                        Map.entry("TZ", "UTC"),
                        Map.entry("SERVER_SERVLET_CONTEXT_PATH", apiPath)
                    )
                )
                .withExposedPorts(8080)
                .waitingFor(
                    Wait.forHttp("/api/actuator/health/readiness")
                        .forPort(8080)
                        .forStatusCode(HttpStatus.SC_OK)
                )
                .withStartupTimeout(Duration.ofMinutes(2));
        }

        return applicationContainer;
    }

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> getPostgresContainer(@Value("${configuration.database.password}") String password,
                                                       @Value("${configuration.database.name}") String databaseName) {
        if (postgreSQLContainer == null) {
            postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6"))
                .withNetwork(dockerNetwork)
                .withNetworkAliases("postgres")
                .withDatabaseName(databaseName)
                .withEnv("POSTGRES_PASSWORD", password)
                .withCommand("-c", "max_connections=1000");
        }

        return postgreSQLContainer;
    }

    @Bean
    @ServiceConnection
    public RedisContainer getRedisContainer() {
        if (redisContainer == null) {
            redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"))
                .withNetwork(Network.newNetwork())
                .withNetworkAliases("redis");
        }

        return redisContainer;
    }
}
