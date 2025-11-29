package dev.retreever.schema.resolver;

import dev.retreever.schema.model.JsonPropertyType;
import dev.retreever.schema.context.ResolverContext;
import dev.retreever.schema.model.Schema;
import dev.retreever.schema.model.ValueSchema;
import dev.retreever.schema.resolver.util.TypeUtils;

import java.lang.reflect.Type;
import java.net.URI;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.UUID;

/**
 * Resolves primitive and primitive-like Java types into {@link ValueSchema}.
 */
public final class ValueSchemaResolver {

    private ValueSchemaResolver() {}

    /**
     * Resolves the given type into a {@link ValueSchema}.
     */
    public static Schema resolve(Type type, ResolverContext ctx) {
        Class<?> raw = TypeUtils.getRawClass(type);
        JsonPropertyType jsonType = mapToJsonType(raw);
        return new ValueSchema(jsonType);
    }

    /**
     * Maps a Java class to the appropriate {@link JsonPropertyType}.
     */
    private static JsonPropertyType mapToJsonType(Class<?> clazz) {

        // Critical: order matters — keep the simple checks first.

        if (clazz.isPrimitive()) {
            if (clazz == boolean.class) return JsonPropertyType.BOOLEAN;
            if (clazz == char.class || clazz == byte.class) return JsonPropertyType.STRING;
            if (clazz == short.class || clazz == int.class || clazz == long.class
                    || clazz == float.class || clazz == double.class) {
                return JsonPropertyType.NUMBER;
            }
        }

        if (Number.class.isAssignableFrom(clazz)) {
            return JsonPropertyType.NUMBER;
        }

        if (CharSequence.class.isAssignableFrom(clazz)) {
            return JsonPropertyType.STRING;
        }

        if (clazz == Boolean.class) {
            return JsonPropertyType.BOOLEAN;
        }

        if (clazz == UUID.class) {
            return JsonPropertyType.UUID;
        }

        if (clazz.isEnum()) {
            return JsonPropertyType.ENUM;
        }

        if (Temporal.class.isAssignableFrom(clazz)) {
            // LocalDate, LocalDateTime, LocalTime, Instant
            if (clazz == LocalDate.class) return JsonPropertyType.DATE;
            if (clazz == LocalTime.class) return JsonPropertyType.TIME;
            if (clazz == LocalDateTime.class || clazz == Instant.class) {
                return JsonPropertyType.DATE_TIME;
            }
            // default fallback
            return JsonPropertyType.STRING;
        }

        if (clazz == Duration.class) {
            return JsonPropertyType.DURATION;
        }

        if (clazz == Period.class) {
            return JsonPropertyType.PERIOD;
        }

        if (clazz == URI.class) {
            return JsonPropertyType.URI;
        }

        // Final fallback
        return JsonPropertyType.STRING;
    }
}
