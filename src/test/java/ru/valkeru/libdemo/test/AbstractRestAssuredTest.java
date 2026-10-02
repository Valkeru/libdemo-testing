package ru.valkeru.libdemo.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import net.javacrumbs.jsonunit.core.Option;
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

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

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

    @Autowired
    @Qualifier("apiUrl")
    private String uri;

    @Autowired
    private AuthenticationUtil authenticationUtil;

    @Autowired
    private RedisUtil redisUtil;

    protected AbstractRestAssuredTest() {
        this.objectMapper = new ObjectMapper();
    }

    @BeforeAll
    @AfterEach
    void clearRedis() {
        redisUtil.clearCaches();
    }

    protected final String readResourceAsString(String path) {
        return FileUtil.readResourceAsString(path);
    }

    protected static void assertJsonContent(String result, String expected) {
        assertThatJson(result).isEqualTo(expected);
    }

    protected static void assertJsonContent(String result, String expected, Option option, Option... otherOptions) {
        assertThatJson(result).when(option, otherOptions).isEqualTo(expected);
    }

    protected final RequestSpecification notAuthenticatedRequest() {
        return RestAssured.given()
            .filter(new AllureRestAssured())
            .baseUri(uri)
            .log()
            .ifValidationFails()
            .accept(APPLICATION_JSON);
    }

    protected final RequestSpecification adminRequest() {
        return authenticatedRequest(authenticationUtil.adminToken());
    }

    protected final RequestSpecification managerRequest() {
        return authenticatedRequest(authenticationUtil.managerToken());
    }

    protected final RequestSpecification userRequest() {
        return authenticatedRequest(authenticationUtil.userToken());
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
