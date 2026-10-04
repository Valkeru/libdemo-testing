package ru.valkeru.libdemo.test.matcher;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

@FunctionalInterface
public interface ResponseMatcher {

    ValidatableResponse match(Response response);
}
