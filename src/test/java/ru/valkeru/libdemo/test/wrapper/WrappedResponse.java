package ru.valkeru.libdemo.test.wrapper;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import ru.valkeru.libdemo.test.matcher.ResponseMatcher;

public class WrappedResponse {

    private final Response response;

    public WrappedResponse(Response response) {
        this.response = response;
    }

    public WrappedResponse match(ResponseMatcher matcher) {
        matcher.match(response);

        return this;
    }

    public Response raw() {
        return response;
    }

    public ValidatableResponse then() {
        return response.then();
    }

    public ExtractableResponse<Response> extract() {
        return response.then().extract();
    }
}
