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

import java.util.stream.Stream;

import static ru.valkeru.libdemo.test.constants.TestConstants.AUTHOR_ID;
import static ru.valkeru.libdemo.test.constants.TestConstants.START_UUID_VALUE;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectCreated;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectForbidden;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNoContent;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNotAuthorized;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNotFound;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectOk;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectBadRequest;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class AuthorServiceTest extends AbstractRestAssuredTest {

    public static final String SERVICE_AUTHOR_PATH = "/service/author";
    public static final String SERVICE_AUTHOR_ID_PATH_W_ID = "/service/author/{id}";

    @ParameterizedTest
    @MethodSource("getBadRequestArguments")
    @DisplayName("Add an author - invalid request")
    void testCreateAuthorBadRequest(String payloadPath, String expectedResultPath) {
        String payload = readResourceAsString(payloadPath);
        String expected = readResourceAsString(expectedResultPath);

        managerRequest()
            .body(payload)
            .post(SERVICE_AUTHOR_PATH)
            .match(expectBadRequest(expected, Option.IGNORING_ARRAY_ORDER));
    }

    @Test
    @DisplayName("Add an author - success")
    void testCreateAuthorOk() {
        String payload = readResourceAsString("json/author/request/add_valid.json");
        String expected = readResourceAsString("json/author/response/created.json");

        String newAuthorPath = managerRequest()
            .body(payload)
            .post(SERVICE_AUTHOR_PATH)
            .match(expectCreated())
            .extract()
            .header(HttpHeaders.LOCATION);

        notAuthenticatedRequest()
            .get(newAuthorPath)
            .match(expectOk(expected));
    }

    @Test
    @DisplayName("Add an author - 401")
    void testCreateAuthorUnauthorized() {
        String payload = readResourceAsString("json/author/request/add_valid.json");

        notAuthenticatedRequest()
            .body(payload)
            .post(SERVICE_AUTHOR_PATH)
            .match(expectNotAuthorized());
    }

    @Test
    @DisplayName("Add an author, librarian - 403")
    void testCreateAuthorForbidden() {
        String payload = readResourceAsString("json/author/request/add_valid.json");

        librarianRequest()
            .body(payload)
            .post(SERVICE_AUTHOR_PATH)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Add an author, user - 403")
    void testCreateAuthorUserForbidden() {
        String payload = readResourceAsString("json/author/request/add_valid.json");

        userRequest()
            .body(payload)
            .post(SERVICE_AUTHOR_PATH)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Update author info - 404")
    void testUpdateAuthorNotFound() {
        String payload = readResourceAsString("json/author/request/update_valid.json");
        String expected = readResourceAsString("json/author/response/not_found.json");

        managerRequest()
            .body(payload)
            .patch(SERVICE_AUTHOR_ID_PATH_W_ID, START_UUID_VALUE)
            .match(expectNotFound(expected));

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

        managerRequest()
            .body(payload)
            .patch(SERVICE_AUTHOR_ID_PATH_W_ID, AUTHOR_ID)
            .match(expectBadRequest(expected, Option.IGNORING_ARRAY_ORDER));
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

        notAuthenticatedRequest()
            .get("/v1/author/{id}", AUTHOR_ID)
            .match(expectOk(initialExpected));

        managerRequest()
            .body(payload)
            .patch(SERVICE_AUTHOR_ID_PATH_W_ID, AUTHOR_ID)
            .match(expectNoContent());

        notAuthenticatedRequest()
            .get("/v1/author/{id}", AUTHOR_ID)
            .match(expectOk(expected));
    }

    @Test
    @DisplayName("Update an author - 401")
    void testUpdateAuthorUnauthorized() {
        String payload = readResourceAsString("json/author/request/update_valid.json");

        notAuthenticatedRequest()
            .body(payload)
            .patch(SERVICE_AUTHOR_ID_PATH_W_ID, START_UUID_VALUE)
            .match(expectNotAuthorized());
    }

    @Test
    @DisplayName("Update an author, librarian - 403")
    void testUpdateAuthorForbidden() {
        String payload = readResourceAsString("json/author/request/update_valid.json");

        librarianRequest()
            .body(payload)
            .patch(SERVICE_AUTHOR_ID_PATH_W_ID, START_UUID_VALUE)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Update an author, user - 403")
    void testUpdateAuthorUserForbidden() {
        String payload = readResourceAsString("json/author/request/update_valid.json");

        userRequest()
            .body(payload)
            .patch(SERVICE_AUTHOR_ID_PATH_W_ID, START_UUID_VALUE)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Delete author info - 404")
    void deleteAuthorNotFound() {
        String expected = readResourceAsString("json/author/response/not_found.json");

        managerRequest()
            .delete(SERVICE_AUTHOR_ID_PATH_W_ID, START_UUID_VALUE)
            .match(expectNotFound(expected));
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql"
        }
    )
    @DisplayName("Delete author info - success")
    void deleteAuthorOk() {
        String existedExpected = readResourceAsString("json/author/response/author.json");
        notAuthenticatedRequest()
            .get("/v1/author/{id}", AUTHOR_ID)
            .match(expectOk(existedExpected));

        managerRequest()
            .delete(SERVICE_AUTHOR_ID_PATH_W_ID, AUTHOR_ID)
            .match(expectNoContent());

        String expected = readResourceAsString("json/author/response/deleted_not_found.json");
        notAuthenticatedRequest()
            .get("/v1/author/{id}", AUTHOR_ID)
            .match(expectNotFound(expected));
    }

    @Test
    @DisplayName("Delete author info - 401")
    void testDeleteAuthorUnauthorized() {
        notAuthenticatedRequest()
            .delete(SERVICE_AUTHOR_ID_PATH_W_ID, START_UUID_VALUE)
            .match(expectNotAuthorized());
    }

    @Test
    @DisplayName("Delete author info, librarian - 403")
    void testDeleteAuthorForbidden() {
        librarianRequest()
            .delete(SERVICE_AUTHOR_ID_PATH_W_ID, START_UUID_VALUE)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    @Test
    @DisplayName("Delete author info, user - 403")
    void testDeleteAuthorUserForbidden() {
        userRequest()
            .delete(SERVICE_AUTHOR_ID_PATH_W_ID, START_UUID_VALUE)
            .match(expectForbidden(getForbiddenExpectedBody()));
    }

    private static Stream<Arguments> getBadRequestArguments() {
        return Stream.of(
            Arguments.of("json/author/request/invalid_no_fields.json", "json/author/response/validation_error.json"),
            Arguments.of("json/author/request/invalid_blank_strings.json", "json/author/response/validation_error.json"),
            Arguments.of("json/author/request/invalid_nulls.json", "json/author/response/validation_error.json")
        );
    }
}
