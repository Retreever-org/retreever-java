/*
 * Copyright (c) 2025 Retreever Contributors
 *
 * Licensed under the MIT License.
 *     https://opensource.org/licenses/MIT
 */

package dev.retreever.schema.resolver;

import dev.retreever.domain.model.JsonProperty;
import dev.retreever.schema.context.ResolvedTypeContext;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for schema resolution.
 * <p>
 * This class:
 * - Creates a fresh ResolvedTypeContext for every resolution
 * - Iterates only over root-level fields
 * - Delegates ALL real work to JsonPropertyResolver
 * </p>
 * JsonSchemaResolver never resolves nested structures by itself.
 */
public class JsonSchemaResolver {

    public List<JsonProperty> resolve(Type type) {

        // Always create fresh context — NO cross-endpoint leakage
        ResolvedTypeContext ctx = new ResolvedTypeContext(type);

        Class<?> raw = TypeResolver.extractRawClass(type);
        if (raw == null) {
            return List.of();
        }

        Field[] fields = raw.getDeclaredFields();

        // ---------- OBJECT ROOT ----------
        if (raw.isRecord() || fields.length > 0) {

            List<JsonProperty> props = new ArrayList<>();

            for (Field field : fields) {
                field.setAccessible(true);

                JsonProperty p = JsonPropertyResolver.resolve(
                        field,
                        field.getGenericType(), // raw generic type
                        ctx                     // context with generic bindings
                );

                props.add(p);
            }

            return props;
        }

        // ---------- ARRAY / SIMPLE ROOT ----------
        return List.of(
                JsonPropertyResolver.resolveElement(type, ctx, "root")
        );
    }

    public List<JsonProperty> resolve(Class<?> clazz) {
        return resolve((Type) clazz);
    }
}
