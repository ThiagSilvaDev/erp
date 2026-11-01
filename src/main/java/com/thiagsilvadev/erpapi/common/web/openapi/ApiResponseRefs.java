package com.thiagsilvadev.erpapi.common.web.openapi;

/** References to reusable OpenAPI error responses registered by the web configuration. */
public final class ApiResponseRefs {

    private static final String PREFIX = "#/components/responses/";

    public static final String BAD_REQUEST = PREFIX + "BadRequest";
    public static final String UNAUTHORIZED = PREFIX + "Unauthorized";
    public static final String FORBIDDEN = PREFIX + "Forbidden";
    public static final String NOT_FOUND = PREFIX + "NotFound";
    public static final String CONFLICT = PREFIX + "Conflict";
    public static final String UNPROCESSABLE_ENTITY = PREFIX + "UnprocessableEntity";

    private ApiResponseRefs() {}
}
