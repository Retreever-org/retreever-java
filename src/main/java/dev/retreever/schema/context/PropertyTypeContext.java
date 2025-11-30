package dev.retreever.schema.context;

import java.lang.reflect.*;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Context responsible for resolving generic type variables (T, U, K, V, etc.)
 * into their concrete runtime Types based on a given root Type.
 * <p>
 * This is used by the schema resolver to correctly expand structures such as:
 * <li>ApiResponse&lt;User&gt;</li>
 * <li>List&lt;Category&gt;</li>
 * <li>Map&lt;String, Product&gt;</li>
 * <li>User&lt;T&gt; where T = Address</li>
 * </p>
 * Once created from a root Type, this context can be used to resolve all
 * nested generic type variables encountered during schema resolution.
 */
public final class PropertyTypeContext {

    /**
     * Maps type variables (e.g., T, U, K) to their actual concrete types.
     */
    private final Map<TypeVariable<?>, Type> resolvedVariables = new IdentityHashMap<>();


    /**
     * Creates a new context for the given root Type.
     * All type parameters found in the root type's hierarchy are mapped once.
     *
     * @param rootType the root Type for resolution
     */
    public PropertyTypeContext(Type rootType) {
        build(rootType);
    }


    /**
     * Recursively maps type variables in the type hierarchy to their actual
     * runtime Types. Handles nested generics and deep chains of parameterized types.
     */
    private void build(Type type) {

        if (!(type instanceof ParameterizedType p)) {
            return;
        }

        Class<?> raw = (Class<?>) p.getRawType();
        TypeVariable<?>[] vars = raw.getTypeParameters();
        Type[] args = p.getActualTypeArguments();

        for (int i = 0; i < vars.length; i++) {
            resolvedVariables.put(vars[i], args[i]);
            build(args[i]); // recursively map deep generics
        }
    }


    /**
     * Resolves a Type into its concrete form, applying type-variable substitutions.
     *
     * @param type the type to resolve
     * @return a fully resolved Type with generics expanded
     */
    public Type resolve(Type type) {

        // T → actual type
        if (type instanceof TypeVariable<?> tv) {
            return resolvedVariables.getOrDefault(tv, Object.class);
        }

        // List<T> → List<User>
        if (type instanceof ParameterizedType p) {
            return resolveParameterizedType(p);
        }

        // T[] → User[]
        if (type instanceof GenericArrayType ga) {
            Type comp = resolve(ga.getGenericComponentType());
            return Array.newInstance((Class<?>) comp, 0).getClass();
        }

        // plain Class or other Type remains unchanged
        return type;
    }


    /**
     * Resolves all type arguments of a parameterized type.
     */
    private ParameterizedType resolveParameterizedType(ParameterizedType original) {

        Type[] args = original.getActualTypeArguments();
        Type[] resolved = new Type[args.length];

        for (int i = 0; i < args.length; i++) {
            resolved[i] = resolve(args[i]);
        }

        return new ResolvedParamType(
                (Class<?>) original.getRawType(),
                resolved
        );
    }


    /**
     * Lightweight implementation of ParameterizedType with resolved arguments.
     */
    private static final class ResolvedParamType implements ParameterizedType {

        private final Class<?> raw;
        private final Type[] args;

        ResolvedParamType(Class<?> raw, Type[] args) {
            this.raw = raw;
            this.args = args;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return args;
        }

        @Override
        public Type getRawType() {
            return raw;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
