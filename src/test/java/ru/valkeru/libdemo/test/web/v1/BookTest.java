package ru.valkeru.libdemo.test.web.v1;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;
import ru.valkeru.libdemo.test.constants.TestConstants;

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
}
