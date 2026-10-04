package ru.valkeru.libdemo.test.web.service;

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

import static ru.valkeru.libdemo.test.constants.TestConstants.SERIES_ID;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectBadRequest;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectConflict;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectCreated;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectForbidden;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNoContent;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNotAuthorized;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNotFound;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectOk;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class SeriesServiceTest extends AbstractRestAssuredTest {

    private static final String SERIES_SERVICE_PATH = "/service/series";
    private static final String SERIES_SERVICE_PATH_W_ID = "/service/series/{id}";

    @ParameterizedTest
    @MethodSource("validationFailedArguments")
    @DisplayName("Create a series - 400")
    void testCreateSeriesBadRequest(String contentPath, String expectedResultPath) {
        String content = readResourceAsString(contentPath);
        String expected = readResourceAsString(expectedResultPath);

        managerRequest()
            .body(content)
            .post(SERIES_SERVICE_PATH)
            .match(expectBadRequest(expected));
    }

    @Test
    @DisplayName("Create a series - 404, no cycle")
    void testCreateSeriesCycleNotFound() {
        String content = readResourceAsString("json/series/request/add_cycle_not_found.json");
        String expected = readResourceAsString("json/series/response/cycle_not_found.json");

        managerRequest()
            .body(content)
            .post(SERIES_SERVICE_PATH)
            .match(expectNotFound(expected));
    }

    @Sql(
        value = {
            "classpath:sql/cycle/truncate.sql",
            "classpath:sql/cycle/insert.sql",
        }
    )
    @ParameterizedTest
    @MethodSource("validArguments")
    @DisplayName("Create a series - OK")
    void testCreateSeriesOk(String contentPath, String expectedPath) {
        String content = readResourceAsString(contentPath);
        String expected = readResourceAsString(expectedPath);

        String createdSeriesPath = managerRequest()
            .body(content)
            .post(SERIES_SERVICE_PATH)
            .match(expectCreated())
            .extract()
            .header(HttpHeaders.LOCATION);

        notAuthenticatedRequest()
            .get(createdSeriesPath)
            .match(expectOk(expected));
    }

    @Test
    @DisplayName("Create a series - 401")
    void testCreateSeriesUnauthorized() {
        String content = readResourceAsString("json/series/request/add_valid_no_cycle.json");

        notAuthenticatedRequest()
            .body(content)
            .post(SERIES_SERVICE_PATH)
            .match(expectNotAuthorized());
    }

    @Test
    @DisplayName("Create a series, librarian - 403")
    void testCreateSeriesForbidden() {
        String content = readResourceAsString("json/series/request/add_valid_no_cycle.json");

        librarianRequest()
            .body(content)
            .post(SERIES_SERVICE_PATH)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Create a series, user - 403")
    void testCreateSeriesForbiddenFailed() {
        String content = readResourceAsString("json/series/request/add_valid_no_cycle.json");

        userRequest()
            .body(content)
            .post(SERIES_SERVICE_PATH)
            .match(expectForbidden(getForbiddenExpectedBody()));
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
    @DisplayName("Update series - 400")
    void testUpdateSeriesBadRequest(String contentPath, String expectedResultPath) {
        String content = readResourceAsString(contentPath);
        String expected = readResourceAsString(expectedResultPath);

        managerRequest()
            .body(content)
            .patch(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .match(expectBadRequest(expected));
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/cycle/truncate.sql",
            "classpath:sql/cycle/insert.sql",
            "classpath:sql/series/insert.sql",
        }
    )
    @DisplayName("Update series - 404, no cycle")
    void testUpdateSeriesCycleNotFound() {
        String content = readResourceAsString("json/series/request/add_cycle_not_found.json");
        String expected = readResourceAsString("json/series/response/cycle_not_found.json");

        managerRequest()
            .body(content)
            .patch(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .match(expectNotFound(expected));
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
    @DisplayName("Update series - OK")
    void testUpdateSeriesOk(String contentPath, String expectedResultPath) {
        String initialExpected = readResourceAsString("json/series/response/series.json");
        String content = readResourceAsString(contentPath);
        String expected = readResourceAsString(expectedResultPath);

        notAuthenticatedRequest()
            .get("/v1/series/{id}", SERIES_ID)
            .match(expectOk(initialExpected));

        managerRequest()
            .body(content)
            .patch(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .match(expectNoContent());

        notAuthenticatedRequest()
            .get("/v1/series/{id}", SERIES_ID)
            .match(expectOk(expected));
    }

    @Test
    @DisplayName("Update series - 401")
    void testUpdateSeriesUnauthorized() {
        String content = readResourceAsString("json/series/request/update_no_cycle.json");

        notAuthenticatedRequest()
            .body(content)
            .patch(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .match(expectNotAuthorized());
    }

    @Test
    @DisplayName("Update series, librarian - 403")
    void testUpdateSeriesForbidden() {
        String content = readResourceAsString("json/series/request/update_no_cycle.json");

        librarianRequest()
            .body(content)
            .patch(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Update series, user - 403")
    void testUpdateSeriesForbiddenFailed() {
        String content = readResourceAsString("json/series/request/update_no_cycle.json");

        userRequest()
            .body(content)
            .patch(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Delete series - 404")
    void testDeleteSeriesNotFound() {
        String expected = readResourceAsString("json/series/response/not_found.json");

        managerRequest()
            .delete(SERIES_SERVICE_PATH_W_ID, TestConstants.START_UUID_VALUE)
            .match(expectNotFound(expected));
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
    @DisplayName("Delete series - 409")
    void testDeleteSeriesConflict() {
        String expected = readResourceAsString("json/conflict.json");

        managerRequest()
            .delete(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .match(expectConflict(expected));
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql"
        }
    )
    @DisplayName("Delete series - OK")
    void testDeleteSeriesOk() {
        String existsExpected = readResourceAsString("json/series/response/series.json");
        String expected = readResourceAsString("json/series/response/deleted_not_found.json");

        notAuthenticatedRequest()
            .get("/v1/series/{id}", SERIES_ID)
            .match(expectOk(existsExpected));

        managerRequest()
            .delete(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .match(expectNoContent());

        notAuthenticatedRequest()
            .get("/v1/series/{id}", SERIES_ID)
            .match(expectNotFound(expected));
    }

    @Test
    @DisplayName("Delete series - 401")
    void testDeleteSeriesUnauthorized() {
        notAuthenticatedRequest()
            .delete(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .match(expectNotAuthorized());
    }

    @Test
    @DisplayName("Delete series, librarian - 403")
    void testDeleteSeriesForbidden() {
        librarianRequest()
            .delete(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Delete series, user - 403")
    void testDeleteSeriesForbiddenFailed() {
        userRequest()
            .delete(SERIES_SERVICE_PATH_W_ID, SERIES_ID)
            .match(expectForbidden(getForbiddenExpectedBody()));
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
