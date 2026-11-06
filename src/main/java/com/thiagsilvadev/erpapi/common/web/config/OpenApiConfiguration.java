package com.thiagsilvadev.erpapi.common.web.config;

import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.BAD_REQUEST;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.CONFLICT;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.FORBIDDEN;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.NOT_FOUND;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNAUTHORIZED;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNPROCESSABLE_ENTITY;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
@OpenAPIDefinition(
        info =
                @Info(
                        title = "ERP API",
                        version = "1.0",
                        description =
                                "REST API for ERP system with JWT authentication and permission-based authorization",
                        contact = @Contact(name = "ThiagSilvaDev")))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Login issues a global token (token_use=global) for company listing and token selection. "
                + "Use select-company to obtain a scoped token (token_use=scoped) for business endpoints, "
                + "or select-platform to obtain a platform token (token_use=platform) for /api/v1/platform/**. "
                + "Operations also enforce their required permissions.")
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components()
                .addSchemas("ProblemDetail", buildProblemDetailSchema())
                .addResponses(responseName(BAD_REQUEST), problemResponse("Bad request"))
                .addResponses(responseName(UNAUTHORIZED), problemResponse("Unauthorized"))
                .addResponses(responseName(FORBIDDEN), problemResponse("Access denied"))
                .addResponses(responseName(NOT_FOUND), problemResponse("Resource not found"))
                .addResponses(responseName(CONFLICT), problemResponse("Conflict"))
                .addResponses(responseName(UNPROCESSABLE_ENTITY), problemResponse("Unprocessable entity"));

        return new OpenAPI()
                .components(components)
                .tags(List.of(
                        new Tag()
                                .name("catalog")
                                .description("Product and category management; requires a scoped token"),
                        new Tag()
                                .name("authentication")
                                .description("Login and token selection; protected operations require a global token"),
                        new Tag().name("users").description("User profile management; requires a scoped token"),
                        new Tag().name("companies").description("Company administration; requires a platform token"),
                        new Tag()
                                .name("company-contacts")
                                .description("Company contact management; requires a scoped token"),
                        new Tag().name("customers").description("Customer management; requires a scoped token"),
                        new Tag().name("suppliers").description("Supplier management; requires a scoped token"),
                        new Tag()
                                .name("contact-persons")
                                .description("Contact person management; requires a scoped token"),
                        new Tag()
                                .name("inventory")
                                .description("Inventory and stock management; requires a scoped token"),
                        new Tag().name("sales").description("Sales order management; requires a scoped token"),
                        new Tag()
                                .name("financial-accounting-accounts")
                                .description("Accounting account management; requires a scoped token"),
                        new Tag()
                                .name("financial-accounting-entries")
                                .description("Accounting postings and balances; requires a scoped token"),
                        new Tag()
                                .name("financial-accounts")
                                .description("Financial accounts and cash movements; requires a scoped token"),
                        new Tag().name("financial-invoices").description("Invoice management; requires a scoped token"),
                        new Tag()
                                .name("financial-obligations")
                                .description("Receivables and payables; requires a scoped token"),
                        new Tag().name("financial-payments").description("Payment management; requires a scoped token"),
                        new Tag()
                                .name("logistics")
                                .description("Driver, vehicle and shipment management; requires a scoped token"),
                        new Tag()
                                .name("driver-mobile")
                                .description("Assigned driver workflows; requires a scoped token"),
                        new Tag().name("iam").description("Tenant roles and memberships; requires a scoped token"),
                        new Tag().name("kpis").description("Executive KPIs; requires a scoped token")));
    }

    private String responseName(String ref) {
        return ref.substring(ref.lastIndexOf('/') + 1);
    }

    private ApiResponse problemResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType(
                                "application/problem+json",
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ProblemDetail"))));
    }

    private Schema<?> buildProblemDetailSchema() {
        return new ObjectSchema()
                .addProperty("type", new StringSchema().format("uri"))
                .addProperty("title", new StringSchema())
                .addProperty("status", new IntegerSchema())
                .addProperty("detail", new StringSchema())
                .addProperty("instance", new StringSchema().format("uri"))
                .addProperty("timestamp", new StringSchema().format("date-time"))
                .addProperty("error_code", new StringSchema().description("Optional domain error code"))
                .addProperty(
                        "invalid_params",
                        new ArraySchema()
                                .items(new ObjectSchema()
                                        .addProperty("name", new StringSchema())
                                        .addProperty("reason", new StringSchema())))
                .additionalProperties(true);
    }
}
