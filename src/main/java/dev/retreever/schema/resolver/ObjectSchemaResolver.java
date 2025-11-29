package dev.retreever.schema.resolver;

import dev.retreever.schema.context.ResolverContext;
import dev.retreever.schema.model.JsonPropertyType;
import dev.retreever.schema.model.ObjectSchema;
import dev.retreever.schema.model.Property;
import dev.retreever.schema.model.Schema;
import dev.retreever.schema.resolver.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * Resolves complex POJO types into {@link ObjectSchema} by inspecting their fields.
 * Recursion is safely handled by returning an empty ObjectSchema.
 */
public final class ObjectSchemaResolver {

    private ObjectSchemaResolver() {}

    public static Schema resolve(Type type, ResolverContext ctx) {

        Class<?> raw = TypeUtils.getRawClass(type);

        // Recursion detection: if we're already resolving this type → {}
        if (ctx.registry().isResolving(type)) {
            return new ObjectSchema(); // empty object placeholder
        }

        // Mark type as resolving
        ctx.registry().markResolving(type);

        ObjectSchema schema = new ObjectSchema();

        // Iterate declared fields
        for (Field field : raw.getDeclaredFields()) {

            // Skip static or transient fields
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }

            // Ensure access
            field.setAccessible(true);

            // Resolve field type via generics context
            Type resolvedFieldType = ctx.typeContext().resolve(field.getGenericType());
            Class<?> fieldRaw = TypeUtils.getRawClass(resolvedFieldType);

            JsonPropertyType propertyType = mapToJsonPropertyType(fieldRaw);

            // Recursion handling for field type
            Schema fieldSchema;
            if (ctx.registry().isResolving(resolvedFieldType)) {
                // Recursive field → placeholder (null schema)
                fieldSchema = null;
            } else {
                // Normal resolution
                fieldSchema = ctx.orchestrator().resolve(resolvedFieldType, ctx);
            }

            // Create property
            Property property = new Property(
                    field.getName(),
                    propertyType,
                    fieldSchema
            );

            // Add to schema
            schema.getProperties().put(field.getName(), property);
        }

        // Done resolving this type
        ctx.registry().unmarkResolving(type);

        return schema;
    }

    /**
     * Map raw Java class to JsonPropertyType category.
     */
    private static JsonPropertyType mapToJsonPropertyType(Class<?> raw) {

        // Array / Collection
        if (raw.isArray()
                || java.util.Collection.class.isAssignableFrom(raw)
                || java.util.stream.Stream.class.isAssignableFrom(raw)) {
            return JsonPropertyType.ARRAY;
        }

        // Map
        if (java.util.Map.class.isAssignableFrom(raw)) {
            return JsonPropertyType.OBJECT; // maps → synthetic object
        }

        // Complex objects → OBJECT
        if (!raw.isEnum()
                && !raw.isPrimitive()
                && !raw.getPackageName().startsWith("java.")) {
            return JsonPropertyType.OBJECT;
        }

        // Fallback for primitives / simple types
        return JsonPropertyType.STRING;
    }
}
