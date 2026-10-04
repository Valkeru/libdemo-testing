package ru.valkeru.libdemo.test.util;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.valkeru.libdemo.test.Role;
import ru.valkeru.libdemo.test.config.ApiProperties;
import ru.valkeru.libdemo.test.constants.TestConstants;
import ru.valkeru.libdemo.test.dto.AuthenticationDto;

import java.util.Map;

@Component
public class AuthenticationUtil {

    private final Map<Role, String> userNames;

    private final String defaultPassword;

    private static final String AUTH_SCHEME = "Bearer";
    private static final String SIGN_IN_PATH = "/security/sign-in";

    private final String apiUrl;

    public AuthenticationUtil(ApiProperties apiProperties, @Qualifier("apiUrl") String apiUrl) {
        this.userNames = apiProperties.username();
        this.defaultPassword = apiProperties.defaultPassword();
        this.apiUrl = apiUrl;


    }

    public String token(Role role) {
        String token = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(new AuthenticationDto(userNames.get(role), defaultPassword))
            .when()
            .post(apiUrl + SIGN_IN_PATH)
            .then()
            .statusCode(HttpStatus.SC_NO_CONTENT)
            .extract()
            .header(TestConstants.ACCESS_TOKEN_HEADER_NAME);

        return "%s %s".formatted(AUTH_SCHEME, token);
    }
}
