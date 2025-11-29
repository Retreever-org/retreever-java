package dev.retreever.repo;

import dev.retreever.schema.model.Schema;

import java.lang.reflect.Type;
import java.util.*;

public final class SchemaRegistry {

    // Cycle detection still MUST use Type identity (correct)
    private final Set<Type> resolving = Collections.newSetFromMap(new IdentityHashMap<>());

    // Cache must use RAW CLASS, not Type
    private final Map<Class<?>, Schema> resolvedSchemas = new HashMap<>();

    // Cycle detection

    public boolean isResolving(Type type) {
        return resolving.contains(type);
    }

    public void markResolving(Type type) {
        resolving.add(type);
    }

    public void unmarkResolving(Type type) {
        resolving.remove(type);
    }

    // Cache

    public boolean hasResolved(Type type) {
        Class<?> raw = dev.retreever.schema.resolver.util.TypeUtils.getRawClass(type);
        return resolvedSchemas.containsKey(raw);
    }

    public Schema getResolved(Type type) {
        Class<?> raw = dev.retreever.schema.resolver.util.TypeUtils.getRawClass(type);
        return resolvedSchemas.get(raw);
    }

    public void saveResolved(Type type, Schema schema) {
        Class<?> raw = dev.retreever.schema.resolver.util.TypeUtils.getRawClass(type);
        if (shouldCache(raw)) {
            resolvedSchemas.put(raw, schema);
        }
    }

    // Cache rules

    private boolean shouldCache(Class<?> clazz) {

        if (clazz.isArray()) return false;
        if (Collection.class.isAssignableFrom(clazz)) return false;
        if (Map.class.isAssignableFrom(clazz)) return false;
        if (clazz.isPrimitive()) return false;

        String pkg = clazz.getPackageName();
        return !pkg.startsWith("java.") && !pkg.startsWith("jakarta.");
    }
}
