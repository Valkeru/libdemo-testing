package ru.valkeru.libdemo.test.web.v1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;
import ru.valkeru.libdemo.test.constants.TestConstants;

import static ru.valkeru.libdemo.test.constants.TestConstants.SERIES_ID;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNotFound;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectOk;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class SeriesTest extends AbstractRestAssuredTest {

    @Test
    @DisplayName("Get series list - empty")
    void testGetSeriesEmptyList() {
        String expected = readResourceAsString("json/empty_list.json");

        notAuthenticatedRequest()
            .get("/v1/series")
            .match(expectOk(expected));
    }

    @Test
    @DisplayName("Get series - not found")
    void testGetSeriesNotFound() {
        String expected = readResourceAsString("json/series/response/not_found.json");

        notAuthenticatedRequest()
            .get("/v1/series/{id}", TestConstants.START_UUID_VALUE)
            .match(expectNotFound(expected));
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/02.create_cycle.sql",
            "classpath:sql/03.create_series.sql"
        }
    )
    @DisplayName("Get series - OK")
    void testGetSeriesOk() {
        String listExpected = readResourceAsString("json/series/response/list.json");
        String seriesExpected = readResourceAsString("json/series/response/series.json");

        notAuthenticatedRequest()
            .get("/v1/series")
            .match(expectOk(listExpected));

        notAuthenticatedRequest()
            .get("/v1/series/{id}", SERIES_ID)
            .match(expectOk(seriesExpected));
    }
}
