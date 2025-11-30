package dev.retreever.schema.context;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * Captures type variable substitutions for generic resolution.
 * Handles both root-level and field-level parameterized types.
 */
public record ResolverContext(Map<TypeVariable<?>, Type> subs) {

    public ResolverContext() {
        this(Map.of());
    }

    /**
     * Substitutes TypeVariables only - no recursion to prevent stack overflow.
     */
    public Type substitute(Type type) {
        if (subs.isEmpty()) return type;
        if (type instanceof TypeVariable<?> tv) {
            return subs.getOrDefault(tv, type);
        }
        return type;
    }

    /**
     * Creates context from root resolution type (ApiResponse<ProductResponse>)
     */
    public static ResolverContext fromRoot(Type rootType) {
        Map<TypeVariable<?>, Type> subs = new HashMap<>();
        captureParameterizedSubs(rootType, subs);
        return new ResolverContext(subs);
    }

    /**
     * Creates context for field resolution with declaring class context.
     * Page<ProductResponse>.content → List<ProductResponse> → captures T→ProductResponse
     */
    public static ResolverContext fromField(Field field, Type declaringType) {
        Map<TypeVariable<?>, Type> subs = new HashMap<>();

        // 1. Declaring class context (Page<T>)
        captureParameterizedSubs(declaringType, subs);

        // 2. Field type context (List<T>)
        captureParameterizedSubs(field.getGenericType(), subs);

        return new ResolverContext(subs);
    }

    /**
     * Merges parent context with new substitutions for nested resolution.
     */
    public ResolverContext merge(ResolverContext other) {
        Map<TypeVariable<?>, Type> combined = new HashMap<>(this.subs);
        combined.putAll(other.subs);
        return new ResolverContext(combined);
    }

    private static void captureParameterizedSubs(Type type, Map<TypeVariable<?>, Type> subs) {
        if (type instanceof ParameterizedType pt) {
            Class<?> rawType = (Class<?>) pt.getRawType();
            TypeVariable<?>[] typeParams = rawType.getTypeParameters();
            Type[] actualArgs = pt.getActualTypeArguments();

            for (int i = 0; i < Math.min(typeParams.length, actualArgs.length); i++) {
                subs.put(typeParams[i], actualArgs[i]);
            }
        }
    }
}
