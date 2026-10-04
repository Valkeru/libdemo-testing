package ru.valkeru.libdemo.test.util;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.valkeru.libdemo.test.constants.TestConstants;
import ru.valkeru.libdemo.test.dto.AuthenticationDto;

@Component
public class AuthenticationUtil {

    private final String userLogin;
    private final String managerLogin;
    private final String librarianLogin;
    private final String adminLogin;

    private final String defaultPassword;

    private static final String AUTH_SCHEME = "Bearer";
    private static final String SIGN_IN_PATH = "/security/sign-in";

    private final String apiUrl;

    public AuthenticationUtil(@Value("${configuration.api.username.user}") String userLogin,
                              @Value("${configuration.api.username.manager}") String managerLogin,
                              @Value("${configuration.api.username.librarian}") String librarianLogin,
                              @Value("${configuration.api.username.admin}") String adminLogin,
                              @Value("${configuration.api.default_password}") String defaultPassword,
                              @Qualifier("apiUrl") String apiUrl) {
        this.userLogin = userLogin;
        this.managerLogin = managerLogin;
        this.librarianLogin = librarianLogin;
        this.adminLogin = adminLogin;
        this.defaultPassword = defaultPassword;
        this.apiUrl = apiUrl;
    }

    public String adminToken() {
        String userToken = getToken(adminLogin);

        return tokenWithScheme(userToken);
    }

    public String librarianToken() {
        String userToken = getToken(librarianLogin);

        return tokenWithScheme(userToken);
    }

    public String managerToken() {
        String userToken = getToken(managerLogin);

        return tokenWithScheme(userToken);
    }

    public String userToken() {
        String userToken = getToken(userLogin);

        return tokenWithScheme(userToken);
    }

    private String getToken(String userName) {
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createAuthenticationDto(userName))
            .when()
            .post(apiUrl + SIGN_IN_PATH)
            .then()
            .extract()
            .header(TestConstants.ACCESS_TOKEN_HEADER_NAME);
    }

    private AuthenticationDto createAuthenticationDto(String userName) {
        return new AuthenticationDto(userName, defaultPassword);
    }

    private static String tokenWithScheme(String token) {
        return "%s %s".formatted(AUTH_SCHEME, token);
    }
}
