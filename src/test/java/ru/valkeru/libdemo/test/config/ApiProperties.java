package ru.valkeru.libdemo.test.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.valkeru.libdemo.test.Role;

import java.util.Map;

@ConfigurationProperties("configuration.api")
public record ApiProperties(String defaultPassword, Map<Role, String> username) {
}
