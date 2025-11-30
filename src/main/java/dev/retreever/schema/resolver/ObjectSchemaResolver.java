/*
 * Copyright (c) 2025 Retreever Contributors
 *
 * Licensed under the MIT License.
 * You may obtain a copy of the License at:
 *     https://opensource.org/licenses/MIT
 */

package dev.retreever.schema.resolver;

import dev.retreever.schema.model.JsonPropertyType;
import dev.retreever.schema.model.ObjectSchema;
import dev.retreever.schema.model.Property;
import dev.retreever.schema.model.Schema;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reflectively resolves a Java {@link Type} into an {@link ObjectSchema} by processing all fields.
 * Uses {@link PropertyResolver} for leaf types and recursive delegation for containers.
 */
public class ObjectSchemaResolver {

    public static Schema resolve(Type type) {
        Class<?> clazz = SchemaResolver.extractRawClass(type);
        if (clazz == null || clazz.isPrimitive() || clazz.isEnum()) {
            return new ObjectSchema();
        }

        ObjectSchema objectSchema = new ObjectSchema();
        Field[] fields = getAllFields(clazz);

        for (Field field : fields) {
            field.setAccessible(true);
            Type fieldType = field.getGenericType();

            // CRITICAL: Use resolveField for ALL nested types
            Schema fieldSchema = SchemaResolver.resolveField(field, clazz, fieldType);

            JsonPropertyType fieldKind = JsonPropertyTypeResolver.resolve(
                    SchemaResolver.extractRawClass(fieldType)
            );

            Property property = new Property(
                    field.getName(),
                    fieldKind,
                    fieldSchema
            );
            objectSchema.addProperty(property);
        }

        return objectSchema.isEmpty() ? new ObjectSchema() : objectSchema;
    }

    /**
     * Collects all declared instance fields from class hierarchy.
     */
    private static Field[] getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields.toArray(new Field[0]);
    }
}
