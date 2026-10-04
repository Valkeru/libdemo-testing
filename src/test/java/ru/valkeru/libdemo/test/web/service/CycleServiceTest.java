package ru.valkeru.libdemo.test.web.service;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;
import ru.valkeru.libdemo.test.constants.TestConstants;

import static org.hamcrest.Matchers.equalTo;
import static ru.valkeru.libdemo.test.constants.TestConstants.CYCLE_ID;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectBadRequest;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectConflict;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectCreated;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectForbidden;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNoContent;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNotAuthorized;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNotFound;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectOk;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class CycleServiceTest extends AbstractRestAssuredTest {

    private static final String CYCLE_SERVICE_PATH = "/service/cycle";
    private static final String CYCLE_SERVICE_PATH_W_ID = "/service/cycle/{id}";

    @Test
    @DisplayName("Create a cycle - 400")
    void testCreateCycleBadRequest() {
        String payload = readResourceAsString("json/cycle/request/cycle_not_valid.json");
        String expected = readResourceAsString("json/cycle/response/validation_failed.json");

        adminRequest()
            .body(payload)
            .post(CYCLE_SERVICE_PATH)
            .match(expectBadRequest(expected));
    }

    @Test
    @DisplayName("Create a cycle - 200")
    void testCreateCycleOk() {
        String payload = readResourceAsString("json/cycle/request/cycle_valid.json");
        String initialExpected = readResourceAsString("json/empty_list.json");
        String resultExpected = readResourceAsString("json/cycle/response/created.json");

        notAuthenticatedRequest()
            .get("/v1/cycle")
            .match(expectOk(initialExpected));

        String newCyclePath = adminRequest()
            .body(payload)
            .post(CYCLE_SERVICE_PATH)
            .match(expectCreated())
            .extract()
            .header(HttpHeaders.LOCATION);

        notAuthenticatedRequest()
            .get(newCyclePath)
            .match(expectOk(resultExpected));
    }

    @Test
    @DisplayName("Create a cycle - 401")
    void testCreateCycleUnauthorized() {
        String payload = readResourceAsString("json/cycle/request/cycle_valid.json");

        notAuthenticatedRequest()
            .body(payload)
            .post(CYCLE_SERVICE_PATH)
            .match(expectNotAuthorized());
    }

    @Test
    @DisplayName("Create a cycle, librarian - 403")
    void testCreateCycleForbidden() {
        String payload = readResourceAsString("json/cycle/request/cycle_valid.json");

        librarianRequest()
            .body(payload)
            .post(CYCLE_SERVICE_PATH)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Create a cycle, user - 403")
    void testCreateCycleForbiddenFailed() {
        String payload = readResourceAsString("json/cycle/request/cycle_valid.json");

        userRequest()
            .body(payload)
            .post(CYCLE_SERVICE_PATH)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Create a cycle - 404")
    void testUpdateCycleNotFound() {
        String payload = readResourceAsString("json/cycle/request/cycle_valid.json");
        String expected = readResourceAsString("json/cycle/response/not_found.json");

        adminRequest()
            .body(payload)
            .patch(CYCLE_SERVICE_PATH_W_ID, TestConstants.START_UUID_VALUE)
            .match(expectNotFound(expected));
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/02.create_cycle.sql"
        }
    )
    @DisplayName("Update a cycle - 200")
    void testUpdateCycleOk() {
        String payload = readResourceAsString("json/cycle/request/cycle_valid.json");
        String expected = readResourceAsString("json/cycle/response/created.json");

        notAuthenticatedRequest()
            .get("/v1/cycle/{id}", CYCLE_ID)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("title", equalTo("test_9411799dad"));

        adminRequest()
            .body(payload)
            .patch(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .match(expectNoContent());

        notAuthenticatedRequest()
            .get("/v1/cycle/{id}", CYCLE_ID)
            .match(expectOk(expected));
    }

    @Test
    @DisplayName("Update a cycle - 401")
    void testUpdateCycleUnauthorized() {
        String payload = readResourceAsString("json/cycle/request/cycle_valid.json");

        notAuthenticatedRequest()
            .body(payload)
            .patch(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .match(expectNotAuthorized());
    }

    @Test
    @DisplayName("Update a cycle, librarian - 403")
    void testUpdateCycleForbidden() {
        String payload = readResourceAsString("json/cycle/request/cycle_valid.json");

        librarianRequest()
            .body(payload)
            .patch(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Update a cycle, user - 403")
    void testUpdateCycleForbiddenFailed() {
        String payload = readResourceAsString("json/cycle/request/cycle_valid.json");

        userRequest()
            .body(payload)
            .patch(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Delete a cycle - 404")
    void testDeleteCycleNotFound() {
        String expected = readResourceAsString("json/cycle/response/not_found.json");

        adminRequest()
            .delete(CYCLE_SERVICE_PATH_W_ID, TestConstants.START_UUID_VALUE)
            .match(expectNotFound(expected));
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql"
        }
    )
    @DisplayName("Delete a cycle - 409, series exists")
    void testDeleteCycleSeriesConflict() {
        String expected = readResourceAsString("json/conflict.json");

        adminRequest()
            .delete(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .match(expectConflict(expected));
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
    @DisplayName("Delete a cycle - 409, books exists")
    void testDeleteCycleBookConflict() {
        String expected = readResourceAsString("json/conflict.json");

        adminRequest()
            .delete(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .match(expectConflict(expected));
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/02.create_cycle.sql"
        }
    )
    @DisplayName("Delete a cycle - OK")
    void testDeleteCycleOk() {
        notAuthenticatedRequest()
            .get("/v1/cycle/{id}", CYCLE_ID)
            .then()
            .statusCode(HttpStatus.SC_OK);

        adminRequest()
            .delete(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .match(expectNoContent());

        String expected = readResourceAsString("json/cycle/response/deleted_not_found.json");
        notAuthenticatedRequest()
            .get("/v1/cycle/{id}", CYCLE_ID)
            .match(expectNotFound(expected));
    }

    @Test
    @DisplayName("Delete a cycle - 401")
    void testDeleteCycleUnauthorized() {
        notAuthenticatedRequest()
            .delete(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .match(expectNotAuthorized());
    }

    @Test
    @DisplayName("Delete a cycle, librarian - 403")
    void testDeleteCycleForbidden() {
        librarianRequest()
            .delete(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Delete a cycle, user - 403")
    void testDeleteCycleForbiddenFailed() {
        userRequest()
            .delete(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }
}
