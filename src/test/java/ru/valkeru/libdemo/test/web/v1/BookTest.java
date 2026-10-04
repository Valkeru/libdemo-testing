package ru.valkeru.libdemo.test.web.v1;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;
import ru.valkeru.libdemo.test.constants.TestConstants;

import java.util.Map;

import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectOk;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class BookTest extends AbstractRestAssuredTest {

    @Test
    void testListBooksEmptyList() {
        String expected = readResourceAsString("json/empty_list.json");

        notAuthenticatedRequest()
            .get("/v1/book")
            .match(expectOk(expected));
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
    void testListBooksOk() {
        String expected = readResourceAsString("json/book/response/list_ok.json");

        notAuthenticatedRequest()
            .get("/v1/book")
            .match(expectOk(expected));
    }

    @Test
    void testGetBookNotFound() {
        notAuthenticatedRequest()
            .get("/v1/book/{id}", TestConstants.START_UUID_VALUE)
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
    void testGetBookOk() {
        String expected = readResourceAsString("json/book/response/get_ok.json");

        notAuthenticatedRequest()
            .get("/v1/book/{id}", TestConstants.BOOK_ID)
            .match(expectOk(expected));
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
    @DisplayName("Search books")
    void testSearchBook() {
        String expected = readResourceAsString("json/book/response/list_ok.json");

        notAuthenticatedRequest()
            .query(Map.of("name", "d2982"))
            .get("/v1/book")
            .match(expectOk(expected));
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
    @DisplayName("Search books with SQL injection - empty list")
    void testGetBookWithInjection() {
        String expected = readResourceAsString("json/empty_list.json");

        notAuthenticatedRequest()
            .query(Map.of("name", "' OR 1=1 --"))
            .get("/v1/book")
            .match(expectOk(expected));
    }
}
