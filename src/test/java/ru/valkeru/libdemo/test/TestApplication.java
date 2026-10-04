package ru.valkeru.libdemo.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import ru.valkeru.libdemo.test.config.ApiProperties;

// required for spring boot
@SpringBootApplication
@EnableConfigurationProperties({
    ApiProperties.class
})
public class TestApplication {

    /**
     * API url for release test (application is started in Docker via Testcontainers)
     */
    @Bean("apiUrl")
    @Profile("test-release")
    public String apiUrlContainer(@Value("${configuration.api.path}") String path, GenericContainer<?> applicationContainer) {
        return "http://localhost:%d%s".formatted(applicationContainer.getMappedPort(8080), path);
    }

    /**
     * API url for application is locally launched or on a remote machine
     */
    @Bean("apiUrl")
    @Profile("!test-release")
    public String apiUrlLocal(
        @Value("${configuration.api.schema}://${configuration.api.host}:${configuration.api.port}${configuration.api.path}")
        String url
    ) {
        return url;
    }
}
