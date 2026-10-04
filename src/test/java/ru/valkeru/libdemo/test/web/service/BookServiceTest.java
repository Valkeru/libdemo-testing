package ru.valkeru.libdemo.test.web.service;

import net.javacrumbs.jsonunit.core.Option;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;
import ru.valkeru.libdemo.test.constants.TestConstants;

import java.util.stream.Stream;

import static ru.valkeru.libdemo.test.constants.TestConstants.BOOK_ID;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectBadRequest;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectCreated;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectForbidden;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNoContent;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNotAuthorized;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNotFound;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectOk;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class BookServiceTest extends AbstractRestAssuredTest {


    @ParameterizedTest
    @MethodSource("badRequestPaths")
    @DisplayName("Create a book - bad request")
    void testCreateBookBadRequest(String requestPath, String responsePath) {
        String payload = readResourceAsString(requestPath);
        String expected = readResourceAsString(responsePath);

        managerRequest()
            .body(payload)
            .post("/service/book")
            .match(expectBadRequest(expected, Option.IGNORING_ARRAY_ORDER));
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
    @DisplayName("Create a book - resource not found")
    void testCreateBookNotFound(String requestPath, String responsePath) {
        String payload = readResourceAsString(requestPath);
        String expected = readResourceAsString(responsePath);

        managerRequest()
            .body(payload)
            .post("/service/book")
            .match(expectNotFound(expected));
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql",
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql"
        }
    )
    @DisplayName("Create a book - success")
    void testCreateBookOk() {
        String payload = readResourceAsString("json/book/request/create_request_valid.json");
        String expected = readResourceAsString("json/book/response/service/book_created.json");

        String createdBookPath = managerRequest()
            .body(payload)
            .post("/service/book")
            .match(expectCreated())
            .extract()
            .header(HttpHeaders.LOCATION);

        notAuthenticatedRequest()
            .get(createdBookPath)
            .match(expectOk(expected));
    }

    @Test
    @DisplayName("Create a book - 401")
    void testCreateBookUnauthorized() {
        String payload = readResourceAsString("json/book/request/create_request_valid.json");

        notAuthenticatedRequest()
            .body(payload)
            .post("/service/book")
            .match(expectNotAuthorized());
    }

    @Test
    @DisplayName("Create a book, librarian - 403")
    void testCreateBookForbidden() {
        String payload = readResourceAsString("json/book/request/create_request_valid.json");

        librarianRequest()
            .body(payload)
            .post("/service/book")
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Create a book, user - 403")
    void testCreateBookUserForbidden() {
        String payload = readResourceAsString("json/book/request/create_request_valid.json");

        userRequest()
            .body(payload)
            .post("/service/book")
            .match(expectForbidden(getForbiddenExpectedBody()));
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
    @DisplayName("Update a book - 400")
    void testUpdateBookBadRequest(String requestPath, String responsePath) {
        String payload = readResourceAsString(requestPath);
        String expected = readResourceAsString(responsePath);

        managerRequest()
            .body(payload)
            .patch("/service/book/{id}", BOOK_ID)
            .match(expectBadRequest(expected, Option.IGNORING_ARRAY_ORDER));
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
    @DisplayName("Update a book - 404")
    void testUpdateBookNotFound(String requestPath, String expectedPath) {
        String payload = readResourceAsString(requestPath);
        String expected = readResourceAsString(expectedPath);

        managerRequest()
            .body(payload)
            .patch("/service/book/{id}", BOOK_ID)
            .match(expectNotFound(expected));
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
    @DisplayName("Update a book - success")
    void testUpdateBookOk() {
        String initialExpected = readResourceAsString("json/book/response/get_ok.json");
        String payload = readResourceAsString("json/book/request/update_valid_request.json");
        String expected = readResourceAsString("json/book/response/book_updated.json");

        notAuthenticatedRequest()
            .get("/v1/book/{id}", BOOK_ID)
            .match(expectOk(initialExpected));

        managerRequest()
            .body(payload)
            .patch("/service/book/{id}", BOOK_ID)
            .match(expectNoContent());

        notAuthenticatedRequest()
            .get("/v1/book/{id}", BOOK_ID)
            .match(expectOk(expected, Option.IGNORING_ARRAY_ORDER));
    }

    @Test
    @DisplayName("Update a book - 401")
    void testUpdateBookUnauthorized() {
        String payload = readResourceAsString("json/book/request/update_valid_request.json");

        notAuthenticatedRequest()
            .body(payload)
            .patch("/service/book/{id}", BOOK_ID)
            .match(expectNotAuthorized());
    }

    @Test
    @DisplayName("Update a book, librarian - 403")
    void testUpdateBookForbidden() {
        String payload = readResourceAsString("json/book/request/update_valid_request.json");

        librarianRequest()
            .body(payload)
            .patch("/service/book/{id}", BOOK_ID)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Update a book, user - 403")
    void testUpdateBookUserForbidden() {
        String payload = readResourceAsString("json/book/request/update_valid_request.json");

        userRequest()
            .body(payload)
            .patch("/service/book/{id}", BOOK_ID)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Delete a book - 404")
    void testDeleteBookNotFound() {
        String expectedBody = readResourceAsString("json/book/response/not_found.json");

        managerRequest()
            .delete("/service/book/{id}", TestConstants.START_UUID_VALUE)
            .match(expectNotFound(expectedBody));
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
    @DisplayName("Delete a book - OK")
    void testDeleteBookOk() {
        String book = readResourceAsString("json/book/response/get_ok.json");
        notAuthenticatedRequest()
            .get("/v1/book/{id}", TestConstants.BOOK_ID)
            .match(expectOk(book));

        managerRequest()
            .delete("/service/book/{id}", TestConstants.BOOK_ID)
            .match(expectNoContent());

        String expected = readResourceAsString("json/book/response/deleted_not_found.json");
        notAuthenticatedRequest()
            .get("/v1/book/{id}", TestConstants.BOOK_ID)
            .match(expectNotFound(expected));
    }

    @Test
    @DisplayName("Delete a book - 401")
    void testDeleteBookUnauthorized() {
        notAuthenticatedRequest()
            .delete("/service/book/{id}", BOOK_ID)
            .match(expectNotAuthorized());
    }

    @Test
    @DisplayName("Delete a book, librarian - 403")
    void testDeleteBookForbidden() {
        librarianRequest()
            .delete("/service/book/{id}", BOOK_ID)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Delete a book, user - 403")
    void testDeleteBookUserForbidden() {
        userRequest()
            .delete("/service/book/{id}", BOOK_ID)
            .match(expectForbidden(getForbiddenExpectedBody()));
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
