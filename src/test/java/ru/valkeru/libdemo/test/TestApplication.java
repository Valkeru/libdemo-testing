package ru.valkeru.libdemo.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;

// required for spring boot
@SpringBootApplication
public class TestApplication {

    @Bean("apiUrl")
    public String apiUrl(@Value("${configuration.api.path}") String path,
                         GenericContainer<?> applicationContainer) {
        return "http://localhost:%d%s".formatted(applicationContainer.getMappedPort(8080), path);
    }
}
