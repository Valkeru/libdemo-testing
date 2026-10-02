package ru.valkeru.libdemo.test.web.service;

import net.javacrumbs.jsonunit.core.Option;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.notNullValue;
import static ru.valkeru.libdemo.test.constants.TestConstants.AUTHOR_ID;
import static ru.valkeru.libdemo.test.constants.TestConstants.START_UUID_VALUE;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class AuthorServiceTest extends AbstractRestAssuredTest {

    @ParameterizedTest
    @MethodSource("getBadRequestArguments")
    @DisplayName("Add an author - invalid request")
    void testCreateAuthorBadRequest(String payloadPath, String expectedResultPath) {
        String payload = readResourceAsString(payloadPath);
        String expected = readResourceAsString(expectedResultPath);

        String response = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(payload)
            .when().post("/service/author")
            .then().assertThat()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(response, expected, Option.IGNORING_ARRAY_ORDER);
    }

    @Test
    @DisplayName("Add an author - success")
    void testCreateAuthorOk() {
        String payload = readResourceAsString("json/author/request/add_valid.json");
        String expected = readResourceAsString("json/author/response/created.json");

        String newAuthorPath = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(payload)
            .when().post("/service/author")
            .then().assertThat()
            .statusCode(HttpStatus.SC_CREATED)
            .header(HttpHeaders.LOCATION, notNullValue())
            .body(emptyString())
            .extract()
            .header(HttpHeaders.LOCATION);

        String response = notAuthenticatedRequest()
            .when().get(newAuthorPath)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(response, expected);
    }

    @Test
    @DisplayName("Update author info - 404")
    void testUpdateAuthorNotFound() {
        String payload = readResourceAsString("json/author/request/update_valid.json");
        String expected = readResourceAsString("json/author/response/not_found.json");

        String response = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(payload)
            .when().patch("/service/author/{id}", START_UUID_VALUE)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .extract().asString();

        assertJsonContent(response, expected);

    }

    @ParameterizedTest
    @MethodSource("getBadRequestArguments")
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql"
        }
    )
    @DisplayName("Update author info - 400")
    void testUpdateAuthorBadRequest(String payloadPath, String expectedResultPath) {
        String payload = readResourceAsString(payloadPath);
        String expected = readResourceAsString(expectedResultPath);

        String response = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(payload)
            .when().patch("/service/author/{id}", AUTHOR_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(response, expected, Option.IGNORING_ARRAY_ORDER);
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql"
        }
    )
    @DisplayName("Update author info - success")
    void testUpdateAuthorOk() {
        String initialExpected = readResourceAsString("json/author/response/author.json");
        String payload = readResourceAsString("json/author/request/update_valid.json");
        String expected = readResourceAsString("json/author/response/updated.json");

        String initialResponse = notAuthenticatedRequest()
            .when().get("/v1/author/{id}", AUTHOR_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .extract().asString();

        assertJsonContent(initialResponse, initialExpected);

        managerRequest()
            .contentType(APPLICATION_JSON)
            .body(payload)
            .when().patch("/service/author/{id}", AUTHOR_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NO_CONTENT)
            .extract().asString();

        String updatedResponse = notAuthenticatedRequest()
            .when().get("/v1/author/{id}", AUTHOR_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .extract().asString();

        assertJsonContent(updatedResponse, expected);
    }

    @Test
    @DisplayName("Delete author info - 404")
    void deleteAuthorNotFound() {
        String expected = readResourceAsString("json/author/response/not_found.json");

        String response = managerRequest()
            .when().delete("/service/author/{id}", START_UUID_VALUE)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .extract().asString();

        assertJsonContent(response, expected);
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql"
        }
    )
    @DisplayName("Delete author info - success")
    void deleteAuthorOk() {
        notAuthenticatedRequest()
            .when().get("/v1/author/{id}", AUTHOR_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK);

        managerRequest()
            .when().delete("/service/author/{id}", AUTHOR_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NO_CONTENT);

        notAuthenticatedRequest()
            .when().get("/v1/author/{id}", AUTHOR_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private static Stream<Arguments> getBadRequestArguments() {
        return Stream.of(
            Arguments.of("json/author/request/invalid_no_fields.json", "json/author/response/validation_error.json"),
            Arguments.of("json/author/request/invalid_blank_strings.json", "json/author/response/validation_error.json"),
            Arguments.of("json/author/request/invalid_nulls.json", "json/author/response/validation_error.json")
        );
    }
}
