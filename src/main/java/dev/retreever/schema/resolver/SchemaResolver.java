/*
 * Copyright (c) 2025 Retreever Contributors
 *
 * Licensed under the MIT License.
 * You may obtain a copy of the License at:
 *     [https://opensource.org/licenses/MIT](https://opensource.org/licenses/MIT)
 */

package dev.retreever.schema.resolver;

import dev.retreever.schema.model.JsonPropertyType;
import dev.retreever.schema.model.Schema;
import dev.retreever.schema.model.ValueSchema;

import java.lang.reflect.*;

/**
 * Central dispatcher for schema resolution using the instance-per-resolution pattern.
 * Delegates to specialized resolvers based on type classification.
 */
public class SchemaResolver {

    private SchemaResolver() {
        // Private constructor to prevent instantiation
    }

    /**
     * Entry point for schema resolution.
     */
    public static Schema resolve(Type type) {
        return new SchemaResolver().resolveSchema(type);
    }

    /**
     * Resolves the given Type into appropriate Schema representation.
     */
    public Schema resolveSchema(Type type) {
        if (type == null) {
            return new ValueSchema(JsonPropertyType.NULL);
        }

        Class<?> rawType = extractRawClass(type);
        JsonPropertyType kind = JsonPropertyTypeResolver.resolve(rawType);

        return switch (kind) {
            case ARRAY -> ArraySchemaResolver.resolve(type);
            case OBJECT -> ObjectSchemaResolver.resolve(type);
            default -> ValueSchemaResolver.resolve(type);
        };
    }

    /**
     * Extracts raw Class from any Type for classification.
     */
    public static Class<?> extractRawClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType pt) {
            Type raw = pt.getRawType();
            if (raw instanceof Class<?> c) {
                return c;
            }
            return Object.class;
        } else if (type instanceof GenericArrayType) {
            return Object[].class;
        } else if (type instanceof TypeVariable<?>) {
            return Object.class;
        } else if (type instanceof WildcardType wt) {
            Type[] upper = wt.getUpperBounds();
            if (upper.length > 0 && upper[0] instanceof Class<?> c) {
                return c;
            }
            return Object.class;
        }
        return Object.class;
    }
}
