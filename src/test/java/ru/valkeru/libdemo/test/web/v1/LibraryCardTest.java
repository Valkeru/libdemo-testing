package ru.valkeru.libdemo.test.web.v1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import ru.valkeru.libdemo.test.AbstractRestAssuredTest;
import ru.valkeru.libdemo.test.constants.TestConstants;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;

import static org.hamcrest.Matchers.notNullValue;
import static ru.valkeru.libdemo.test.matcher.LibraryMatcher.expectCreated;

@Sql(
    value = {
        "classpath:sql/card/00-truncate.sql"
    }
)
@Sql(
    value = {
        "classpath:sql/users/user/reset_role_to_user.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class LibraryCardTest extends AbstractRestAssuredTest {

    @Test
    @Sql(
        value = {
            "classpath:sql/users/user/reset_role_to_user.sql"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Create library card for user - success")
    void testCreateLibraryCardSuccess() {
        String accessToken = userRequest()
            .post("/v1/library-card")
            .match(expectCreated())
            .then()
            .header(TestConstants.ACCESS_TOKEN_HEADER_NAME, notNullValue())
            .header(TestConstants.REFRESH_TOKEN_HEADER_NAME, notNullValue())
            .extract()
            .header(TestConstants.ACCESS_TOKEN_HEADER_NAME);

        // Get and decode JWT payload
        String[] parts = accessToken.split("\\.");
        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);

        JsonNode node = new ObjectMapper().readTree(payload);

        Assertions.assertTrue(node.hasNonNull("role"));
        Assertions.assertEquals("READER", node.get("role").asString());
    }
}
