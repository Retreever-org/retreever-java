package dev.retreever.schema.resolver;

import dev.retreever.domain.model.JsonProperty;
import dev.retreever.domain.model.JsonPropertyType;
import dev.retreever.schema.context.ResolvedTypeContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class JsonArrayResolver {

    private final ResolvedTypeContext ctx;

    public JsonArrayResolver(ResolvedTypeContext ctx) {
        this.ctx = ctx;
    }

    public JsonProperty resolve(Type type, String fieldName) {

        // 1. Determine the actual element type (T from List<T>, or V from Map<K, V>)
        Type elementType = extractElement(type);

        if (elementType != null) {
            // 2. Create the outer ARRAY property shell
            JsonProperty arrayProp = JsonProperty.of(fieldName, JsonPropertyType.ARRAY);

            // 3. Recursively resolve the element type schema using JsonPropertyResolver
            // FIX: Use "items" as the property name for the element schema to conform
            // to JSON Schema structure, regardless of the outer fieldName.
            JsonProperty elementSchema = JsonPropertyResolver.resolveElement(elementType, ctx, "items");

            // 4. Attach the resolved element schema to the array shell
            arrayProp.arrayElement(elementSchema);
            return arrayProp;
        }

        // If T[] (Java array syntax)
        if (type instanceof Class<?> c && c.isArray()) {
            // 2. Create the outer ARRAY property shell
            JsonProperty arrayProp = JsonProperty.of(fieldName, JsonPropertyType.ARRAY);

            // 3. Recursively resolve the element type
            Type componentType = c.getComponentType();
            // FIX: Use "items" as the property name for the element schema.
            JsonProperty elementSchema = JsonPropertyResolver.resolveElement(componentType, ctx, "items");

            // 4. Attach
            arrayProp.arrayElement(elementSchema);
            return arrayProp;
        }

        // Fallback for types not recognized as a schema array (e.g., pure Object.class)
        return JsonProperty.of(fieldName, JsonPropertyType.OBJECT);
    }

    private Type extractElement(Type type) {
        if (type instanceof ParameterizedType p) {
            Class<?> rawType = (Class<?>) p.getRawType();
            Type[] args = p.getActualTypeArguments();

            // Handles List<T>, Set<T>, Optional<T>, Stream<T> (Single generic argument)
            if (Collection.class.isAssignableFrom(rawType) || args.length == 1) {
                // Use ctx.resolve to find the concrete type for T, addressing nested generics
                return ctx.resolve(args[0]);
            }

            // Handles Map<K, V> (Two generic arguments: we want the value V, which is index 1)
            if (Map.class.isAssignableFrom(rawType) && args.length == 2) {
                return ctx.resolve(args[1]);
            }
        }
        return null;
    }
}