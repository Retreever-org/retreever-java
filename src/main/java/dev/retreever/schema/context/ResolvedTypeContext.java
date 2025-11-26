package dev.retreever.schema.context;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ResolvedTypeContext {

    // Maps Type Variables (T) to Actual Types (String, User, etc.) established at the root level.
    private final Map<TypeVariable<?>, Type> resolved = new HashMap<>();

    // Tracks the reference names of types currently being processed in the call stack for recursion guard.
    private final Set<String> resolvingRefNames = new HashSet<>();

    public ResolvedTypeContext(Type rootType) {
        build(rootType);
    }

    /**
     * Recursively maps all TypeVariables found in the type hierarchy of the root type
     * to their actual concrete Type arguments. This map is built once.
     */
    private void build(Type type) {
        if (!(type instanceof ParameterizedType p)) return;

        Class<?> raw = (Class<?>) p.getRawType();
        TypeVariable<?>[] vars = raw.getTypeParameters();
        Type[] args = p.getActualTypeArguments();

        for (int i = 0; i < vars.length; i++) {
            // Map the TypeVariable (e.g., T from ApiResponse<T>) to the concrete Type (e.g., List<ProductResponse>)
            resolved.put(vars[i], args[i]);

            // Recursively call build on the argument type to resolve deep nested generics
            build(args[i]);
        }
    }

    /**
     * Resolve a type variable into its actual type or recursively resolve
     * the arguments of a parameterized type. This is the primary generics lookup method.
     */
    public Type resolve(Type type) {
        if (type instanceof TypeVariable<?> tv) {
            // Case 1: Type is a TypeVariable (e.g., the field 'T' in ApiResponse<T>).
            // Look up its concrete type from the map built at the root.
            return resolved.getOrDefault(tv, Object.class);
        }

        if (type instanceof ParameterizedType p) {
            // Case 2: Type is a Parameterized Type (e.g., List<T> inside the structure).
            Type[] args = p.getActualTypeArguments();
            Type[] resolvedArgs = new Type[args.length];

            for (int i = 0; i < args.length; i++) {
                // Recursively call resolve on the argument type. If args[i] is a TypeVariable,
                // the recursive call will resolve it via Case 1.
                resolvedArgs[i] = resolve(args[i]);
            }

            // Return a new ParameterizedType representation with resolved arguments for reflection.
            return new ResolvedParamType(
                    (Class<?>) p.getRawType(),
                    resolvedArgs
            );
        }

        // Case 3: Simple Class or other Type, returned unchanged.
        return type;
    }

    /**
     * Retrieves the set of type reference names currently active in the resolution stack
     * for recursion protection.
     * @return The set of actively resolving type names.
     */
    public Set<String> getResolvingNames() {
        return resolvingRefNames;
    }
}