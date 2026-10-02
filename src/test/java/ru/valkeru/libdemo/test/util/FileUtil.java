package ru.valkeru.libdemo.test.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@UtilityClass
public class FileUtil {

    public String readResourceAsString(final String path) {
        return StringUtils.toEncodedString(readResourceAsByteArray(path), StandardCharsets.UTF_8);
    }

    public byte[] readResourceAsByteArray(final String path) {
        try {
            return Files.readAllBytes(Paths.get(new ClassPathResource(path).getURI()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
