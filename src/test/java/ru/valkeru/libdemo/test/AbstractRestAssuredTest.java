package ru.valkeru.libdemo.test;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.http.Header;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.valkeru.libdemo.test.constants.TestConstants;
import ru.valkeru.libdemo.test.util.AuthenticationUtil;
import ru.valkeru.libdemo.test.util.FileUtil;
import ru.valkeru.libdemo.test.util.RedisUtil;
import ru.valkeru.libdemo.test.wrapper.WrappedRequestSpecification;

import java.util.Set;

@SpringBootTest(classes = TestApplication.class)
@Sql(
    value = "classpath:sql/delete/00.truncate.sql"
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestingApplicationConfiguration.class)
public abstract class AbstractRestAssuredTest {

    protected static final String APPLICATION_JSON = "application/json";

    private final String forbiddenExpectedBody;

    @Autowired
    @Qualifier("apiUrl")
    private String uri;

    @Autowired
    private AuthenticationUtil authenticationUtil;

    @Autowired
    private RedisUtil redisUtil;

    static {
        // Add headers for masking in report
        LogConfig logConfig = RestAssured.config
            .getLogConfig()
            .blacklistHeader(
                TestConstants.ACCESS_TOKEN_HEADER_NAME,
                TestConstants.REFRESH_TOKEN_HEADER_NAME
            );

        RestAssured.config = RestAssured.config().logConfig(logConfig);
    }

    protected AbstractRestAssuredTest() {
        this.forbiddenExpectedBody = readResourceAsString("json/access_denied.json");
    }

    @BeforeAll
    @AfterEach
    void clearRedis() {
        redisUtil.clearCaches();
    }

    public String getForbiddenExpectedBody() {
        return forbiddenExpectedBody;
    }

    protected static String readResourceAsString(String path) {
        return FileUtil.readResourceAsString(path);
    }


    protected final WrappedRequestSpecification adminRequest() {
        RequestSpecification specification = authenticatedRequest(authenticationUtil.token(Role.ADMIN));

        return new WrappedRequestSpecification(specification);
    }

    protected final WrappedRequestSpecification managerRequest() {
        RequestSpecification specification = authenticatedRequest(authenticationUtil.token(Role.MANAGER));

        return new WrappedRequestSpecification(specification);
    }

    protected final WrappedRequestSpecification librarianRequest() {
        RequestSpecification specification = authenticatedRequest(authenticationUtil.token(Role.LIBRARIAN));

        return new WrappedRequestSpecification(specification);
    }

    protected final WrappedRequestSpecification userRequest() {
        RequestSpecification specification = authenticatedRequest(authenticationUtil.token(Role.USER));

        return new WrappedRequestSpecification(specification);
    }

    protected final WrappedRequestSpecification notAuthenticatedRequest() {
        RequestSpecification specification = RestAssured.given()
            .filter(new AllureRestAssured())
            .baseUri(uri)
            .log()
            .ifValidationFails()
            .accept(APPLICATION_JSON);

        return new WrappedRequestSpecification(specification);
    }

    private RequestSpecification authenticatedRequest(String token) {
        return RestAssured.given()
            .filter(new AllureRestAssured())
            .baseUri(uri)
            .header(new Header(HttpHeaders.AUTHORIZATION, token))
            .accept(APPLICATION_JSON)
            .log()
            .ifValidationFails();
    }
}
