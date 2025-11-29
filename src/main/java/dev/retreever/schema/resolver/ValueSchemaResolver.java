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

import java.lang.reflect.Type;

/**
 * Resolves primitive/atomic types into ValueSchema representations.
 * <p>
 * Used for method return types, top-level primitives, or any non-field atomic values.
 */
public class ValueSchemaResolver {

    /**
     * Resolves primitive/atomic types into ValueSchema.
     *
     * @param type the primitive/atomic type (String, Number, UUID, etc.)
     * @return ValueSchema with resolved JsonPropertyType
     */
    public static Schema resolve(Type type) {
        if (type == null) {
            return new ValueSchema(JsonPropertyType.NULL);
        }

        Class<?> rawType = SchemaResolver.extractRawClass(type);
        JsonPropertyType propType = JsonPropertyTypeResolver.resolve(rawType);

        return new ValueSchema(propType);
    }
}
