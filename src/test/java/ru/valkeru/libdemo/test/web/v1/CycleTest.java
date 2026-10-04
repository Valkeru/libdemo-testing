package ru.valkeru.libdemo.test.web.v1;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;
import ru.valkeru.libdemo.test.constants.TestConstants;

import static ru.valkeru.libdemo.test.constants.TestConstants.CYCLE_ID;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectNotFound;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectOk;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class CycleTest extends AbstractRestAssuredTest {

    @Test
    void testGetCycleNotFound() {
        String expected = readResourceAsString("json/cycle/response/not_found.json");

        notAuthenticatedRequest()
            .get("/v1/cycle/{cycleId}", TestConstants.START_UUID_VALUE)
            .match(expectNotFound(expected));
    }

    @Test
    @Sql(
        value = {
            "classpath:sql/02.create_cycle.sql"
        }
    )
    void testGetCyclesOk() {
        String expected = readResourceAsString("json/cycle/response/get_ok.json");

        notAuthenticatedRequest()
            .get("/v1/cycle/{cycleId}", CYCLE_ID)
            .match(expectOk(expected));
    }
}
