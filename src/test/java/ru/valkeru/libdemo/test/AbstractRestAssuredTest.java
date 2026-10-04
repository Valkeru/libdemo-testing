package ru.valkeru.libdemo.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.valkeru.libdemo.test.util.AuthenticationUtil;
import ru.valkeru.libdemo.test.util.FileUtil;
import ru.valkeru.libdemo.test.util.RedisUtil;
import ru.valkeru.libdemo.test.wrapper.WrappedRequestSpecification;

@SpringBootTest
@Sql(
    value = "classpath:sql/delete/00.truncate.sql"
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestingApplicationConfiguration.class)
public class AbstractRestAssuredTest {

    protected static final String APPLICATION_JSON = "application/json";

    @Getter
    private final ObjectMapper objectMapper;

    @Getter
    private final String forbiddenExpectedBody;

    @Autowired
    @Qualifier("apiUrl")
    private String uri;

    @Autowired
    private AuthenticationUtil authenticationUtil;

    @Autowired
    private RedisUtil redisUtil;

    protected AbstractRestAssuredTest() {
        this.objectMapper = new ObjectMapper();
        this.forbiddenExpectedBody = readResourceAsString("json/access_denied.json");
    }

    @BeforeAll
    @AfterEach
    void clearRedis() {
        redisUtil.clearCaches();
    }

    protected static String readResourceAsString(String path) {
        return FileUtil.readResourceAsString(path);
    }

    protected final WrappedRequestSpecification adminRequest() {
        RequestSpecification specification = authenticatedRequest(authenticationUtil.adminToken());

        return new WrappedRequestSpecification(specification);
    }

    protected final WrappedRequestSpecification managerRequest() {
        RequestSpecification specification = authenticatedRequest(authenticationUtil.managerToken());

        return new WrappedRequestSpecification(specification);
    }

    protected final WrappedRequestSpecification librarianRequest() {
        RequestSpecification specification = authenticatedRequest(authenticationUtil.librarianToken());

        return new WrappedRequestSpecification(specification);
    }

    protected final WrappedRequestSpecification userRequest() {
        RequestSpecification specification = authenticatedRequest(authenticationUtil.userToken());

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
