package ru.valkeru.libdemo.test.web.v1;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;
import ru.valkeru.libdemo.test.constants.TestConstants;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class BookTest extends AbstractRestAssuredTest {

    @Test
    void testListBooksEmptyList() {
        String expected = readResourceAsString("json/empty_list.json");

        String response = notAuthenticatedRequest()
            .when().get("/v1/books")
            .then()
            .statusCode(HttpStatus.SC_OK)
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
    void testListBooksOk() {
        String expected = readResourceAsString("json/book/response/list_ok.json");

        String response = notAuthenticatedRequest()
            .when().get("/v1/books")
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .extract().asString();

        assertJsonContent(response, expected);
    }

    @Test
    void testGetBookNotFound() {
        notAuthenticatedRequest()
            .when().get("/v1/books/{id}", TestConstants.START_UUID_VALUE)
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

        String response = notAuthenticatedRequest()
            .when().get("/v1/books/{id}", TestConstants.BOOK_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .extract().asString();

        assertJsonContent(response, expected);
    }
}
