package ru.valkeru.libdemo.test.web.service;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;
import ru.valkeru.libdemo.test.constants.TestConstants;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.notNullValue;
import static ru.valkeru.libdemo.test.constants.TestConstants.SERIES_ID;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class SeriesServiceTest extends AbstractRestAssuredTest {

    private static final String SERIES_SERVICE_PATH = "/service/series";
    private static final String SERIES_SERVICE_PATH_W_ID = "/service/series/{id}";
    
    @ParameterizedTest
    @MethodSource("validationFailedArguments")
    void testCreateSeriesBadRequest(String contentPath, String expectedResultPath) {
        String content = readResourceAsString(contentPath);
        String expected = readResourceAsString(expectedResultPath);

        String response = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(content)
            .when().post(SERIES_SERVICE_PATH)
            .then().assertThat()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(response, expected);
    }

    @Test
    void testCreateSeriesCycleNotFound() {
        String content = readResourceAsString("json/series/request/add_cycle_not_found.json");
        String expected = readResourceAsString("json/series/response/cycle_not_found.json");

        String response = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(content)
            .when().post(SERIES_SERVICE_PATH)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(response, expected);
    }

    @Sql(
        value = {
            "classpath:sql/cycle/truncate.sql",
            "classpath:sql/cycle/insert.sql",
        }
    )
    @ParameterizedTest
    @MethodSource("validArguments")
    void testCreateSeriesOk(String contentPath, String expectedPath) {
        String content = readResourceAsString(contentPath);
        String expected = readResourceAsString(expectedPath);

        String createdSeriesPath = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(content)
            .when().post(SERIES_SERVICE_PATH)
            .then().assertThat()
            .statusCode(HttpStatus.SC_CREATED)
            .body(emptyString())
            .header(HttpHeaders.LOCATION, notNullValue())
            .extract()
            .header(HttpHeaders.LOCATION);

        String response = notAuthenticatedRequest()
            .when().get(createdSeriesPath)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(response, expected);
    }

    @ParameterizedTest
    @MethodSource("validationFailedArguments")
    @Sql(
        value = {
            "classpath:sql/cycle/truncate.sql",
            "classpath:sql/cycle/insert.sql",
            "classpath:sql/series/insert.sql",
        }
    )
    void testUpdateSeriesBadRequest(String contentPath, String expectedResultPath) {
        String content = readResourceAsString(contentPath);
        String expected = readResourceAsString(expectedResultPath);

        String response = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(content)
            .when().patch(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(response, expected);
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/cycle/truncate.sql",
            "classpath:sql/cycle/insert.sql",
            "classpath:sql/series/insert.sql",
        }
    )
    void testUpdateSeriesCycleNotFound() {
        String content = readResourceAsString("json/series/request/add_cycle_not_found.json");
        String expected = readResourceAsString("json/series/response/cycle_not_found.json");

        String response = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(content)
            .when().patch(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(response, expected);
    }

    @ParameterizedTest
    @MethodSource("updateValidArguments")
    @Sql(
        value = {
            "classpath:sql/cycle/truncate.sql",
            "classpath:sql/cycle/insert.sql",
            "classpath:sql/series/insert.sql",
        }
    )
    void testUpdateSeriesOk(String contentPath, String expectedResultPath) {
        String initialExpected = readResourceAsString("json/series/request/series.json");
        String content = readResourceAsString(contentPath);
        String expected = readResourceAsString(expectedResultPath);

        String initialState = notAuthenticatedRequest()
            .when().get("/v1/series/{id}", SERIES_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(initialState, initialExpected);

        managerRequest()
            .contentType(APPLICATION_JSON)
            .body(content)
            .when().patch(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NO_CONTENT)
            .contentType(emptyString())
            .body(emptyString());

        String updated = notAuthenticatedRequest()
            .when().get("/v1/series/{id}", SERIES_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(updated, expected);
    }

    @Test
    void testDeleteSeriesNotFound() {
        String expected = readResourceAsString("json/series/response/not_found.json");

        String response = managerRequest()
            .when().delete(SERIES_SERVICE_PATH_W_ID, TestConstants.START_UUID_VALUE)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(response, expected);
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql",
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql",
            "classpath:sql/04.create_book.sql",
            "classpath:sql/05.book_to_series.sql"
        }
    )
    void testDeleteSeriesConflict() {
        String expected = readResourceAsString("json/conflict.json");

        String response = managerRequest()
            .when().delete(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_CONFLICT)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(response, expected);
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql"
        }
    )
    void testDeleteSeriesOk() {
        notAuthenticatedRequest()
            .when().get("/v1/series/{id}", SERIES_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK);

        managerRequest()
            .when().delete(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NO_CONTENT)
            .contentType(emptyString())
            .extract().asString();

        notAuthenticatedRequest()
            .when().get("/v1/series/{id}", SERIES_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private static Stream<Arguments> validationFailedArguments() {
        return Stream.of(
            Arguments.of("json/series/request/add_title_blank.json", "json/series/response/validation_error.json"),
            Arguments.of("json/series/request/add_title_null.json", "json/series/response/validation_error.json")
        );
    }

    private static Stream<Arguments> validArguments() {
        return Stream.of(
            Arguments.of("json/series/request/add_valid_no_cycle.json", "json/series/response/created_no_cycle.json"),
            Arguments.of("json/series/request/add_valid_with_cycle.json", "json/series/response/created_with_cycle.json")
        );
    }

    private static Stream<Arguments> updateValidArguments() {
        return Stream.of(
            Arguments.of("json/series/request/update_no_cycle.json", "json/series/response/updated_no_cycle.json"),
            Arguments.of("json/series/request/update_with_cycle.json", "json/series/response/updated_with_cycle.json")
        );
    }
}
