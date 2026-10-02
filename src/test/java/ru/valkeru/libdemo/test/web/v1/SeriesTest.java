package ru.valkeru.libdemo.test.web.v1;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static ru.valkeru.libdemo.test.constants.TestConstants.SERIES_ID;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class SeriesTest extends AbstractRestAssuredTest {

    @Test
    void testGetSeriesNotFound() {
        notAuthenticatedRequest()
            .when().get("/v1/series")
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .body("content", empty());
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql"
        }
    )
    void testGetSeriesOk() {
        notAuthenticatedRequest()
            .when().get("/v1/series")
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .body("content", hasSize(2));

        notAuthenticatedRequest()
            .when().get("/v1/series/{id}", SERIES_ID)
            .then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .body("title", equalTo("test_42db2cab8e"));
    }
}
