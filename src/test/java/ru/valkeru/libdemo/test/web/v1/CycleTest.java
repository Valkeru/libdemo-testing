package ru.valkeru.libdemo.test.web.v1;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;
import ru.valkeru.libdemo.test.constants.TestConstants;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static ru.valkeru.libdemo.test.constants.TestConstants.CYCLE_ID;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class CycleTest extends AbstractRestAssuredTest {

    private static final String CYCLE_SERVICE_PATH = "/service/cycle";
    private static final String CYCLE_SERVICE_PATH_W_ID = "/service/cycle/{id}";

    @Test
    void testCreateCycleBadRequest() {
        adminRequest()
            .body("""
                  {
                    "title": null
                  }
                """)
            .contentType(APPLICATION_JSON)
            .when().post(CYCLE_SERVICE_PATH)
            .then().assertThat()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void testCreateCycleOk() {
        notAuthenticatedRequest()
            .when().get("/v1/cycle")
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .body("content", empty());

        String newCyclePath = adminRequest()
            .contentType(APPLICATION_JSON)
            .body("""
                  {
                    "title": "test_a480bc0a5a"
                  }
                """)
            .when().post(CYCLE_SERVICE_PATH)
            .then().assertThat()
            .statusCode(HttpStatus.SC_CREATED)
            .header(HttpHeaders.LOCATION, notNullValue())
            .body(emptyString())
            .extract()
            .header(HttpHeaders.LOCATION);

        notAuthenticatedRequest()
            .when().get(newCyclePath)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .body("title", response -> equalTo("test_a480bc0a5a"));
    }

    @Test
    void testGetCycleNotFound() {
        notAuthenticatedRequest()
            .when().get("/v1/cycle/{cycleId}", TestConstants.START_UUID_VALUE)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/02.create_cycle.sql"
        }
    )
    void testGetCyclesOk() {
        notAuthenticatedRequest()
            .when().get("/v1/cycle/{cycleId}", CYCLE_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .body("id", equalTo(CYCLE_ID))
            .body("title", equalTo("test_9411799dad"));
    }

    @Test
    void testUpdateCycleNotFound() {
        String expected = readResourceAsString("json/cycle/response/not_found.json");

        String response = adminRequest()
            .body("""
                {
                  "title": "test"
                }
                """)
            .contentType(APPLICATION_JSON)
            .when().patch(CYCLE_SERVICE_PATH_W_ID, TestConstants.START_UUID_VALUE)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .extract().asString();

        assertJsonContent(response, expected);
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/02.create_cycle.sql"
        }
    )
    void testUpdateCycleOk() {
        notAuthenticatedRequest()
            .when().get("/v1/cycle/{id}", CYCLE_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .body("title", equalTo("test_9411799dad"));

        adminRequest()
            .contentType(APPLICATION_JSON)
            .body("""
                    {
                     "title": "test_778a3b8b2f"
                    }
                    """
            )
            .when().patch(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NO_CONTENT);

        notAuthenticatedRequest()
            .when().get("/v1/cycle/{id}", CYCLE_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .body("title", equalTo("test_778a3b8b2f"));
    }

    @Test
    void testDeleteCycleNotFound() {
        adminRequest()
            .when().delete(CYCLE_SERVICE_PATH_W_ID, TestConstants.START_UUID_VALUE)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql"
        }
    )
    void testDeleteCycleSeriesConflict() {
        adminRequest()
            .when().delete(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_CONFLICT);
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
    void testDeleteCycleBookConflict() {
        adminRequest()
            .when().delete(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/02.create_cycle.sql"
        }
    )
    void testDeleteCycleOk() {
        notAuthenticatedRequest()
            .when().get("/v1/cycle/{id}", CYCLE_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK);

        adminRequest()
            .when().delete(CYCLE_SERVICE_PATH_W_ID, CYCLE_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NO_CONTENT);

        notAuthenticatedRequest()
            .when().get("/v1/cycle/{id}", CYCLE_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
