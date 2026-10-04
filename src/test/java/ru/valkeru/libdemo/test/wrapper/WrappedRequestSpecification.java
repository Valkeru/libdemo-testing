package ru.valkeru.libdemo.test.wrapper;

import io.restassured.specification.RequestSpecification;

public final class WrappedRequestSpecification {

    private static final String APPLICATION_JSON = "application/json";

    private final RequestSpecification specification;

    public WrappedRequestSpecification(RequestSpecification specification) {
        this.specification = specification;
    }

    public WrappedRequestSpecification body(String body) {
        return body(body, APPLICATION_JSON);
    }

    public WrappedRequestSpecification body(String body, String contentType) {
        specification
            .body(body)
            .contentType(contentType);

        return this;
    }
    
    public WrappedResponse get(String path) {
        return new WrappedResponse(specification.get(path));
    }

    /**
     * GET particular resource
     *
     * @param pathTemplate resource path template formatted like /resource/{id}
     * @param resourceId Resource ID to build URL
     * @return Response
     */
    public WrappedResponse get(String pathTemplate, String resourceId) {
        return new WrappedResponse(specification.get(pathTemplate, resourceId));
    }

    public WrappedResponse post(String path) {
        return new WrappedResponse(specification.post(path));
    }

    /**
     * PATCH particular resource
     *
     * @param pathTemplate resource path template formatted like /resource/{id}
     * @param resourceId Resource ID to build URL
     * @return Response
     */
    public WrappedResponse patch(String pathTemplate, String resourceId) {
        return new WrappedResponse(specification.patch(pathTemplate, resourceId));
    }

    /**
     * DELETE particular resource
     *
     * @param pathTemplate resource path template formatted like /resource/{id}
     * @param resourceId Resource ID to build URL
     * @return Response
     */
    public WrappedResponse delete(String pathTemplate, String resourceId) {
        return new WrappedResponse(specification.delete(pathTemplate, resourceId));
    }
}
