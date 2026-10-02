package ru.valkeru.libdemo.test.web.service;

import net.javacrumbs.jsonunit.core.Option;
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
import static ru.valkeru.libdemo.test.constants.TestConstants.BOOK_ID;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class BookServiceTest extends AbstractRestAssuredTest {


    @ParameterizedTest
    @MethodSource("badRequestPaths")
    void testCreateBookBadRequest(String requestPath, String responsePath) {
        String payload = readResourceAsString(requestPath);
        String expected = readResourceAsString(responsePath);

        String response = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(payload)
            .when().post("/service/book")
            .then().assertThat()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .extract().asString();

        assertJsonContent(response, expected, Option.IGNORING_ARRAY_ORDER);
    }

    @Sql(
        value = {
            "classpath:sql/01.create_author.sql",
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql",
            "classpath:sql/04.create_book.sql"
        }
    )
    @ParameterizedTest
    @MethodSource("notFoundPaths")
    void testCreateBookNotFound(String requestPath, String responsePath) {
        String payload = readResourceAsString(requestPath);
        String expected = readResourceAsString(responsePath);

        String response = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(payload)
            .when().post("/service/book")
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .extract().asString();

        assertJsonContent(response, expected);
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql",
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql"
        }
    )
    void testCreateBookOk() {
        String payload = readResourceAsString("json/book/request/create_request_valid.json");

        String createdBookPath = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(payload)
            .when().post("/service/book")
            .then().assertThat()
            .statusCode(HttpStatus.SC_CREATED)
            .header(HttpHeaders.LOCATION, notNullValue())
            .body(emptyString())
            .extract()
            .header(HttpHeaders.LOCATION);

        String expected = readResourceAsString("json/book/response/service/book_created.json");
        String response = notAuthenticatedRequest()
            .when().get(createdBookPath)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(response, expected);
    }

    @ParameterizedTest
    @MethodSource("badRequestPaths")
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql",
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql",
            "classpath:sql/04.create_book.sql"
        }
    )
    void testUpdateBookBadRequest(String requestPath, String responsePath) {
        String payload = readResourceAsString(requestPath);
        String expected = readResourceAsString(responsePath);

        String response = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(payload)
            .when().patch("/service/book/{id}", BOOK_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(response, expected, Option.IGNORING_ARRAY_ORDER);
    }

    @Sql(
        value = {
            "classpath:sql/01.create_author.sql",
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql",
            "classpath:sql/04.create_book.sql"
        }
    )
    @ParameterizedTest
    @MethodSource("notFoundPaths")
    void testUpdateBookNotFound(String requestPath, String expectedPath) {
        String payload = readResourceAsString(requestPath);
        String expected = readResourceAsString(expectedPath);

        String response = managerRequest()
            .contentType(APPLICATION_JSON)
            .body(payload)
            .when().patch("/service/book/{id}", BOOK_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .extract().asString();

        assertJsonContent(response, expected);

    }

    @Test
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql",
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql",
            "classpath:sql/04.create_book.sql"
        }
    )
    void testUpdateBookOk() {
        String initialExpected = readResourceAsString("json/book/response/get_ok.json");
        String payload = readResourceAsString("json/book/request/update_valid_request.json");
        String expected = readResourceAsString("json/book/response/book_updated.json");

        String initialState = notAuthenticatedRequest()
            .when().get("/v1/books/{id}", BOOK_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(initialState, initialExpected);

        managerRequest()
            .contentType(APPLICATION_JSON)
            .body(payload)
            .when().patch("/service/book/{id}", BOOK_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NO_CONTENT);

        String updatedState = notAuthenticatedRequest()
            .when().get("/v1/books/{id}", BOOK_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .contentType(APPLICATION_JSON)
            .extract().asString();

        assertJsonContent(updatedState, expected, Option.IGNORING_ARRAY_ORDER);
    }


    @Test
    void testDeleteBookNotFound() {
        managerRequest()
            .when().delete("/service/book/{id}", TestConstants.START_UUID_VALUE)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql",
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql",
            "classpath:sql/04.create_book.sql"
        }
    )
    void testDeleteBookOk() {
        notAuthenticatedRequest()
            .when().get("/v1/books/{id}", TestConstants.BOOK_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK);

        managerRequest()
            .when().delete("/service/book/{id}", TestConstants.BOOK_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NO_CONTENT);

        notAuthenticatedRequest()
            .when().get("/v1/books/{id}", TestConstants.BOOK_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    /**
     * Bad request files paths. Order is "request, response"
     */
    private static Stream<Arguments> badRequestPaths() {
        return Stream.of(
            Arguments.of(
                "json/book/request/create_invalid_request.json",
                "json/book/response/service/create_validation_failed.json"
            )
        );
    }

    /**
     * Not found files paths. Order is "request, response"
     */
    private static Stream<Arguments> notFoundPaths() {
        return Stream.of(
            Arguments.of(
                "json/book/request/not_existed_author_id.json",
                "json/author/response/not_found.json"
            ),
            Arguments.of(
                "json/book/request/not_existed_series_id.json",
                "json/series/response/not_found.json"
            ),
            Arguments.of(
                "json/book/request/not_existed_cycle_id.json",
                "json/cycle/response/not_found.json"
            )
        );
    }
}
