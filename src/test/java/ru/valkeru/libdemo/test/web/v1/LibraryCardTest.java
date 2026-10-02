package ru.valkeru.libdemo.test.web.v1;

import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;
import ru.valkeru.libdemo.test.ApiConfig;

import java.io.IOException;
import java.util.Base64;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.notNullValue;

@Sql(
    value = {
        "classpath:sql/card/00-truncate.sql"
    }
)
@Sql(
    value = {
        "classpath:sql/users/user/reset_role_to_user.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class LibraryCardTest extends AbstractRestAssuredTest {

    @Test
    @Sql(
        value = {
            "classpath:sql/users/user/reset_role_to_user.sql"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Create library card for user - success")
    @Severity(SeverityLevel.BLOCKER)
    void testCreateLibraryCardSuccess() throws IOException {
        ExtractableResponse<Response> extractable = userRequest()
            .basePath("/v1/library-card")
            .noContentType()
            .when().post()
            .then().assertThat()
            .statusCode(HttpStatus.SC_CREATED)
            .body(emptyString())
            .header(ApiConfig.ACCESS_TOKEN_HEADER_NAME, notNullValue())
            .header(ApiConfig.REFRESH_TOKEN_HEADER_NAME, notNullValue())
            .extract();

        // Get and decode JWT payload
        String accessToken = extractable.header(ApiConfig.ACCESS_TOKEN_HEADER_NAME);
        String[] parts = accessToken.split("\\.");
        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);

        JsonNode node = getObjectMapper().readTree(payload);

        Assertions.assertTrue(node.hasNonNull("role"));
        Assertions.assertEquals("READER", node.get("role").asText());
    }
}
