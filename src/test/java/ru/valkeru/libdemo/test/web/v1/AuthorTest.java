package ru.valkeru.libdemo.test.web.v1;

import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;
import ru.valkeru.libdemo.test.constants.TestConstants;

import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNotFound;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectOk;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class AuthorTest extends AbstractRestAssuredTest {

    @Test
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql"
        }
    )
    @DisplayName("Get author info by ID - success")
    void testGetAuthorOk() {
        String expected = readResourceAsString("json/author/response/author.json");

        notAuthenticatedRequest()
            .get("/v1/author/{id}", TestConstants.AUTHOR_ID)
            .match(expectOk(expected));
    }

    @Test
    @DisplayName("Get author info by ID - not found")
    void testGetAuthorNotFound() {
        String expected = readResourceAsString("json/author/response/not_found.json");

        notAuthenticatedRequest()
            .get("/v1/author/{id}", TestConstants.START_UUID_VALUE)
            .match(expectNotFound(expected));
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/01.create_author.sql"
        }
    )
    @DisplayName("Get authors list")
    void testAuthorsListOk() {
        String expected = readResourceAsString("json/author/response/list.json");

        notAuthenticatedRequest()
            .get("/v1/author")
            .match(expectOk(expected, Option.IGNORING_ARRAY_ORDER));
    }
}
