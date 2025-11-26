/*
 * Copyright (c) 2025 Retreever Contributors
 *
 * Licensed under the MIT License.
 * You may obtain a copy of the License at:
 * https://opensource.org/licenses/MIT
 */

package dev.retreever.schema.resolver;

import dev.retreever.schema.context.ResolvedTypeContext;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

/**
 * Utility methods for extracting raw classes and generic type parameters.
 * Used throughout schema resolution to unwrap {@link Type} instances
 * into concrete classes and generate reference names for nested generics.
 * Includes static helpers for recursion tracking.
 */
public class TypeResolver {

    private TypeResolver() {
        // Utility class
    }

    // --- RECURSION TRACKING METHODS ---

    /**
     * Checks if a type is currently in the process of being resolved higher up the stack.
     * Prevents infinite recursion when handling circular references (e.g., A refers to B, B refers to A).
     * @param ctx The current resolution context, which holds the tracker.
     * @param type The type to check (resolved generics).
     * @return true if recursion is detected.
     */
    public static boolean isTypeResolving(ResolvedTypeContext ctx, Type type) {
        // Only complex types (which map to JSON objects) can cause infinite recursion cycles
        Class<?> rawClass = extractRawClass(type);
        if (rawClass != null && isComplexType(rawClass)) {
            String refName = resolveRefName(type);
            // Assuming ctx.getResolvingNames() returns the Set<String> of active names.
            Set<String> resolvingNames = ctx.getResolvingNames();
            return resolvingNames != null && resolvingNames.contains(refName);
        }
        return false;
    }

    /**
     * Marks a complex type as actively resolving by adding its reference name to the context tracker.
     * @param ctx The current resolution context.
     * @param type The type to mark (resolved generics).
     */
    public static void markTypeResolving(ResolvedTypeContext ctx, Type type) {
        Class<?> rawClass = extractRawClass(type);
        if (rawClass != null && isComplexType(rawClass)) {
            String refName = resolveRefName(type);
            Set<String> resolvingNames = ctx.getResolvingNames();
            if (resolvingNames != null) {
                resolvingNames.add(refName);
            }
        }
    }

    /**
     * Unmarks a complex type after its resolution is complete.
     * @param ctx The current resolution context.
     * @param type The type to unmark (resolved generics).
     */
    public static void unmarkTypeResolving(ResolvedTypeContext ctx, Type type) {
        Class<?> rawClass = extractRawClass(type);
        if (rawClass != null && isComplexType(rawClass)) {
            String refName = resolveRefName(type);
            Set<String> resolvingNames = ctx.getResolvingNames();
            if (resolvingNames != null) {
                resolvingNames.remove(refName);
            }
        }
    }

    // --- REFERENCE NAME AND TYPE UTILITIES ---

    /**
     * Produces a stable reference name for a type, incorporating generic arguments.
     * e.g., {@code ApiResponse<PageResponse<User>>} -> "ApiResponse.PageResponse.User"
     */
    public static String resolveRefName(Type type) {
        if (type instanceof Class<?> clazz) {
            // Use simple name for clean references
            return clazz.getSimpleName();
        }

        if (type instanceof ParameterizedType p) {
            Type raw = p.getRawType();
            String rawName = ((Class<?>) raw).getSimpleName();

            Type[] args = p.getActualTypeArguments();
            if (args.length == 0) return rawName;

            StringBuilder sb = new StringBuilder(rawName);
            for (Type arg : args) {
                sb.append(".").append(resolveRefName(arg));
            }
            return sb.toString();
        }

        // Fallback for types that don't fit standard Class or ParameterizedType
        return type.getTypeName();
    }

    /**
     * Extracts the first type argument from a ParameterizedType (e.g., T from List<T>).
     */
    public static Type getTypeParameter(Type type) {
        if (type instanceof ParameterizedType paramType) {
            Type[] typeArgs = paramType.getActualTypeArguments();
            return typeArgs.length > 0 ? typeArgs[0] : null;
        } else return null;
    }

    /**
     * Extracts the raw, non-generic Class from any Type object.
     */
    public static Class<?> extractRawClass(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }

        if (type instanceof ParameterizedType pType) {
            Type raw = pType.getRawType();
            if (raw instanceof Class<?> rc) return rc;
        }

        if (type instanceof TypeVariable<?>) {
            // When unresolved, treat as generic object, or try upper bounds
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            return extractRawClass(bounds.length > 0 ? bounds[0] : Object.class);
        }

        if (type instanceof WildcardType wc) {
            Type[] upper = wc.getUpperBounds();
            return extractRawClass(upper.length > 0 ? upper[0] : Object.class);
        }

        if (type instanceof GenericArrayType ga) {
            Type component = ga.getGenericComponentType();
            Class<?> compClass = extractRawClass(component);
            if (compClass != null) {
                return Array.newInstance(compClass, 0).getClass();
            }
        }

        return Object.class;
    }

    /**
     * Checks if a class is considered a simple type (primitive, wrapper, String, Date, UUID, etc.)
     * which maps directly to a JSON primitive (string, number, boolean).
     */
    public static boolean isKnownSimpleType(Class<?> rawType) {
        if (rawType.isPrimitive() || rawType.isEnum()) {
            return true;
        }

        return rawType.equals(String.class) ||
                rawType.equals(Integer.class) ||
                rawType.equals(Long.class) ||
                rawType.equals(Double.class) ||
                rawType.equals(Float.class) ||
                rawType.equals(Boolean.class) ||
                rawType.equals(Character.class) ||
                rawType.equals(Short.class) ||
                rawType.equals(Byte.class) ||
                rawType.equals(UUID.class) ||
                Date.class.isAssignableFrom(rawType);
    }

    /**
     * Checks if a class represents a collection (List, Set, or their derivatives).
     */
    public static boolean isCollection(Class<?> rawType) {
        return Collection.class.isAssignableFrom(rawType);
    }

    /**
     * Checks if a class is a complex type, meaning it should be treated as a JSON object
     * with properties (fields). This excludes simple types, arrays, collections, and maps.
     */
    public static boolean isComplexType(Class<?> rawType) {
        return !isKnownSimpleType(rawType) &&
                !isCollection(rawType) &&
                !Map.class.isAssignableFrom(rawType) &&
                !rawType.isArray() &&
                !rawType.equals(Object.class);
    }


    /**
     * Checks if a type is a Map (Map, HashMap, etc.).
     */
    public static boolean isMap(Type type) {
        Class<?> rawClass = extractRawClass(type);
        return Map.class.isAssignableFrom(rawClass);
    }

    /**
     * Extracts the value type (V) from a Map<K, V> type.
     */
    public static Type getMapValueType(Type type) {
        if (!(type instanceof ParameterizedType p)) {
            return Object.class;
        }

        Type[] args = p.getActualTypeArguments();
        if (args.length != 2) {
            return Object.class;
        }

        return args[1]; // Index 1 is the value type V
    }
}