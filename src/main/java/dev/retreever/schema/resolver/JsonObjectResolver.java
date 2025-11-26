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
 * Expands OBJECT types by iterating over their declared fields and delegating
 * each field to {@link JsonPropertyResolver}.
 * <p>
 * This resolver:
 * - does NOT handle metadata
 * - does NOT deal with generics by itself
 * - does NOT detect recursion
 * - does NOT filter domain models
 * </p>
 * It ONLY expands the object’s fields.
 */
public class JsonObjectResolver {

    private final ResolvedTypeContext ctx;

    public JsonObjectResolver(ResolvedTypeContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Resolve an OBJECT type into a list of JsonProperty children.
     */
    public List<JsonProperty> resolve(Type type) {

        // resolve generic type variables (T → Actual)
        Type resolvedType = ctx.resolve(type);
        Class<?> raw = TypeResolver.extractRawClass(resolvedType);

        if (raw == null) {
            return List.of();
        }

        Field[] fields = raw.getDeclaredFields();
        if (fields.length == 0) {
            return List.of();
        }

        List<JsonProperty> props = new ArrayList<>(fields.length);

        for (Field field : fields) {
            field.setAccessible(true);

            Type fType = field.getGenericType();

            // DELEGATE — the brain
            JsonProperty property = JsonPropertyResolver.resolve(
                    field,
                    fType,   // raw generic type
                    ctx      // context for resolving T
            );

            props.add(property);
        }

        return props;
    }
}
