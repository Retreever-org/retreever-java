/*
 * Copyright (c) 2025 Retreever Contributors
 *
 * Licensed under the MIT License.
 * You may obtain a copy of the License at:
 *     https://opensource.org/licenses/MIT
 */

package dev.retreever.schema.resolver;

import dev.retreever.schema.context.ResolverContext;
import dev.retreever.schema.model.ArraySchema;
import dev.retreever.schema.model.Schema;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ArraySchemaResolver {

    public static Schema resolve(Type type) {
        ResolverContext ctx = SchemaResolver.CONTEXT.get();
        Type elementType = ctx.substitute(extractElementType(type));
        Schema elementSchema = SchemaResolver.resolve(elementType);
        return new ArraySchema(elementSchema);
    }

    private static Type extractElementType(Type type) {
        if (type instanceof Class<?> cls && cls.isArray()) {
            return cls.getComponentType();
        } else if (type instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            return args.length > 0 ? args[0] : Object.class;
        } else if (type instanceof GenericArrayType at) {
            return at.getGenericComponentType();
        }
        return Object.class;
    }
}
