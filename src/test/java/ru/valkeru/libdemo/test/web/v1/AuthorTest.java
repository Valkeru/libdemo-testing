package ru.valkeru.libdemo.test.web.v1;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;
import ru.valkeru.libdemo.test.constants.TestConstants;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class AuthorTest extends AbstractRestAssuredTest {

    @Test
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql"
        }
    )
    @DisplayName("Get author info by ID - success")
    @Severity(SeverityLevel.BLOCKER)
    void testGetAuthorOk() {
        String expected = readResourceAsString("json/author/response/author.json");

        String response = notAuthenticatedRequest()
            .basePath("/v1/author/{id}")
            .pathParams("id", TestConstants.AUTHOR_ID)
            .when()
            .get()
            .then()
            .assertThat()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .asString();

        assertJsonContent(response, expected);
    }

    @Test
    @DisplayName("Get author info by ID - not found")
    @Severity(SeverityLevel.CRITICAL)
    void testGetAuthorNotFound() {
        String expected = readResourceAsString("json/author/response/not_found.json");

        String response = notAuthenticatedRequest()
            .when().get("/v1/author/{id}", TestConstants.START_UUID_VALUE)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .extract()
            .asString();

        assertJsonContent(response, expected);
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql"
        }
    )
    @DisplayName("Get authors list")
    @Severity(SeverityLevel.BLOCKER)
    void testAuthorsListOk() {
        String expected = readResourceAsString("json/author/response/list.json");

        String response = notAuthenticatedRequest()
            .when()
            .get("/v1/author")
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .asString();

        assertJsonContent(response, expected);
    }
}
