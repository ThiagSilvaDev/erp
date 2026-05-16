package com.thiagsilvadev.erpapi.common.web.config;

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
                        description = "REST API for ERP system with JWT authentication and role-based authorization",
                        contact = @Contact(name = "ThiagSilvaDev")))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Provide the JWT token obtained from POST /api/v1/auth/login")
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components()
                .addSchemas("ProblemDetail", buildProblemDetailSchema())
                .addResponses("BadRequest", problemResponse("Bad request"))
                .addResponses("Unauthorized", problemResponse("Unauthorized"))
                .addResponses("Forbidden", problemResponse("Access denied"))
                .addResponses("NotFound", problemResponse("Resource not found"))
                .addResponses("Conflict", problemResponse("Conflict"))
                .addResponses("UnprocessableEntity", problemResponse("Unprocessable entity"));

        return new OpenAPI()
                .components(components)
                .tags(List.of(
                        new Tag().name("catalog").description("Product and Category management"),
                        new Tag().name("authentication").description("Identity and Access Management"),
                        new Tag().name("user").description("User profile management"),
                        new Tag().name("company").description("Company management endpoints"),
                        new Tag().name("customers").description("Customer management endpoints"),
                        new Tag().name("suppliers").description("Supplier management endpoints"),
                        new Tag().name("contact-persons").description("Contact person management endpoints"),
                        new Tag().name("inventory").description("Inventory and stock management endpoints"),
                        new Tag().name("sales").description("Sales order management endpoints"),
                        new Tag().name("financial").description("Operational finance endpoints")));
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
                .addProperty("timestamp", new StringSchema().format("date-time"));
    }
}
