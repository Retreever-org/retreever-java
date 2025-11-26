package dev.retreever.schema.resolver;

import dev.retreever.domain.model.JsonProperty;

import java.lang.reflect.Field;

/**
 * Resolves SIMPLE / PRIMITIVE-like fields.
 */
public final class JsonSimplePropResolver {

    private JsonSimplePropResolver() {
        // utility class
    }

    public static void resolve(JsonProperty prop, Field field) {
        // Constraints: @Min, @Max, @Size, @Pattern, etc.
        JsonPropertyConstraintResolver.resolve(prop, field);

        // Description: @Schema(description = "...")
        JsonPropertyDescriptionResolver.resolve(prop, field);

        // Example: @Schema(example = "...")
        JsonPropertyExampleResolver.resolve(prop, field);
    }
}
