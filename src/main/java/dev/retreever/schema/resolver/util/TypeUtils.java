package dev.retreever.schema.resolver.util;

import org.springframework.http.ResponseEntity;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utility helpers for inspecting and unwrapping Java {@link Type}s.
 * Focused on exactly what the schema resolver requires:
 * <ul>
 *     <li>Detect raw class from any Type</li>
 *     <li>Check for Optional, ResponseEntity</li>
 *     <li>Identify array, collection, stream, and map types</li>
 *     <li>Extract generic element types</li>
 * </ul>
 */
public final class TypeUtils {

    private TypeUtils() {
    }

     // RAW CLASS EXTRACTION

    /**
     * Returns the raw {@link Class} for any Type.
     * Supports Class, ParameterizedType, and GenericArrayType.
     */
    public static Class<?> getRawClass(Type type) {
        if (type instanceof Class<?> c) {
            return c;
        }
        if (type instanceof ParameterizedType p) {
            return (Class<?>) p.getRawType();
        }
        if (type instanceof GenericArrayType ga) {
            Type component = ga.getGenericComponentType();
            Class<?> rawComponent = getRawClass(component);
            return Array.newInstance(rawComponent, 0).getClass();
        }
        throw new IllegalArgumentException("Unsupported Type: " + type);
    }


     // OPTIONAL

    public static boolean isOptional(Type type) {
        return getRawClass(type) == Optional.class;
    }

    public static Type unwrapOptional(Type type) {
        ParameterizedType p = (ParameterizedType) type;
        return p.getActualTypeArguments()[0];
    }


     // RESPONSE ENTITY

    public static boolean isResponseEntity(Type type) {
        return getRawClass(type) == ResponseEntity.class;
    }

    public static Type unwrapResponseEntity(Type type) {
        ParameterizedType p = (ParameterizedType) type;
        return p.getActualTypeArguments()[0];
    }


     // COLLECTION + STREAM

    public static boolean isCollection(Type type) {
        Class<?> raw = getRawClass(type);
        return Collection.class.isAssignableFrom(raw);
    }

    public static boolean isStream(Type type) {
        return Stream.class.isAssignableFrom(getRawClass(type));
    }

    public static boolean isArray(Type type) {
        return getRawClass(type).isArray();
    }

    /**
     * Extracts element type from List<T>, Set<T>, Stream<T>, or T[].
     */
    public static Type getArrayElementType(Type type) {
        // List<T> / Set<T> / Stream<T>
        if (type instanceof ParameterizedType p) {
            return p.getActualTypeArguments()[0];
        }

        // T[]
        Class<?> raw = getRawClass(type);
        if (raw.isArray()) {
            return raw.getComponentType();
        }

        throw new IllegalArgumentException("Not an array/collection/stream: " + type);
    }


     // MAP

    public static boolean isMap(Type type) {
        return Map.class.isAssignableFrom(getRawClass(type));
    }

    /**
     * Returns key type and value type for Map<K, V>.
     * Only supports ParameterizedType maps.
     */
    public static Type getMapKeyType(Type type) {
        if (!(type instanceof ParameterizedType p)) {
            return Object.class;
        }
        return p.getActualTypeArguments()[0];
    }

    public static Type getMapValueType(Type type) {
        if (!(type instanceof ParameterizedType p)) {
            return Object.class;
        }
        return p.getActualTypeArguments()[1];
    }


    /* ------------------------------------------------------------
     * MISC
     * ------------------------------------------------------------ */

    /**
     * True if the class is from java.*, javax.*, jakarta.* packages.
     */
    public static boolean isPlatformType(Class<?> clazz) {
        String pkg = clazz.getPackageName();
        return pkg.startsWith("java.")
                || pkg.startsWith("javax.")
                || pkg.startsWith("jakarta.");
    }
}
