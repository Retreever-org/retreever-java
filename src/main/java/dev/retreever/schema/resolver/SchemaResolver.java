/*
 * Copyright (c) 2025 Retreever Contributors
 *
 * Licensed under the MIT License.
 * You may obtain a copy of the License at:
 *     https://opensource.org/licenses/MIT
 */

package dev.retreever.schema.resolver;

import dev.retreever.schema.model.Schema;

import java.lang.reflect.Type;

public class SchemaResolver {

    private SchemaResolver() {
        // Private constructor to prevent instantiation
    }

    public static Schema resolve(Type type) {
        return new SchemaResolver().resolveSchema(type);
    }

    public Schema resolveSchema(Type type) {
        // Case 1: type is Class<?>

        // Case 2: type is ParameterizedType

        // Case 3: type is GenericArrayType

        // Case 4: type is TypeVariable<? extends User> -> resolvable

        return null;
    }
}
