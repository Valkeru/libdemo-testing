package ru.valkeru.libdemo.test.matcher;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import net.javacrumbs.jsonunit.ConfigurableJsonMatcher;
import net.javacrumbs.jsonunit.core.Option;
import org.apache.http.HttpStatus;
import ru.valkeru.libdemo.test.constants.TestConstants;

import java.util.Arrays;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.notNullValue;

public final class LibraryMatcher {
    
    private LibraryMatcher(){}

    public static ResponseMatcher expectOk(String expectedBody, Option... bodyComparisonOptions) {
        return response -> expectJson(response, HttpStatus.SC_OK, expectedBody, bodyComparisonOptions);
    }

    public static ResponseMatcher expectCreated() {
        return response -> expectResult(
            response,
            HttpStatus.SC_CREATED
        )
            .body(emptyString());
    }

    public static ResponseMatcher expectNoContent() {
        return response -> expectResult(response, HttpStatus.SC_NO_CONTENT)
            .body(emptyString());
    }

    public static ResponseMatcher expectBadRequest(String expectedBody, Option... bodyComparisonOptions) {
        return response -> expectJson(response, HttpStatus.SC_BAD_REQUEST, expectedBody, bodyComparisonOptions);
    }

    public static ResponseMatcher expectNotFound(String expectedBody) {
        return response -> expectJson(response, HttpStatus.SC_NOT_FOUND, expectedBody);
    }

    public static ResponseMatcher expectNotAuthorized() {
        return response -> expectResult(response, HttpStatus.SC_UNAUTHORIZED);
    }

    public static ResponseMatcher expectForbidden(String expectedBody) {
        return response -> expectJson(response, HttpStatus.SC_FORBIDDEN, expectedBody);
    }

    public static ResponseMatcher expectConflict(String expectedBody) {
        return response -> expectJson(response, HttpStatus.SC_CONFLICT, expectedBody);
    }

    private static ValidatableResponse expectJson(Response response, int statusCode, String expected, Option... options) {
        int optionsCount = options.length;
        ConfigurableJsonMatcher<Object> baseMatcher = jsonEquals(expected);

        return expectResult(response, statusCode)
            .body(
                optionsCount == 0
                    ? baseMatcher
                    : (
                    optionsCount == 1
                        ? baseMatcher.when(options[0])
                        : baseMatcher.when(options[0], Arrays.copyOfRange(options, 1, optionsCount))
                )
            );
    }

    private static ValidatableResponse expectResult(Response response, int statusCode) {
        return response
            .then().assertThat()
            .statusCode(statusCode)
            .header(TestConstants.REQUEST_ID_HEADER_NAME, notNullValue());
    }
}
