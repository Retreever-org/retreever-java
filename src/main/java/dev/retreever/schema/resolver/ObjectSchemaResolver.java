/*
 * Copyright (c) 2025 Retreever Contributors
 *
 * Licensed under the MIT License.
 * You may obtain a copy of the License at:
 *     [https://opensource.org/licenses/MIT](https://opensource.org/licenses/MIT)
 */

package dev.retreever.schema.resolver;

import dev.retreever.schema.model.JsonPropertyType;
import dev.retreever.schema.model.ObjectSchema;
import dev.retreever.schema.model.Property;
import dev.retreever.schema.model.Schema;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reflectively resolves a Java {@link Type} into an {@link ObjectSchema} by processing all fields.
 * Uses {@link PropertyResolver} for leaf types and recursive delegation for containers.
 */
public class ObjectSchemaResolver {

    /**
     * Resolves the given type into an ObjectSchema by reflecting all fields.
     *
     * @param type the Java type to resolve (Class<?> or ParameterizedType)
     * @return ObjectSchema with all field properties resolved
     */
    public static Schema resolve(Type type) {
        Class<?> clazz = extractClass(type);
        if (clazz == null || clazz.isPrimitive() || clazz.isEnum()) {
            return new ObjectSchema(); // Empty fallback
        }

        ObjectSchema objectSchema = new ObjectSchema();
        Field[] fields = getAllFields(clazz);

        for (Field field : fields) {
            field.setAccessible(true);

            // Check field type FIRST - no exceptions needed
            Class<?> fieldClass = field.getType();
            JsonPropertyType fieldType = JsonPropertyTypeResolver.resolve(fieldClass);

            if (fieldType == JsonPropertyType.ARRAY || fieldType == JsonPropertyType.OBJECT) {
                // Container field - recurse
                Type fieldGenericType = field.getGenericType();
                Schema nestedSchema = SchemaResolver.resolve(fieldGenericType);
                Property wrapper = new Property(
                        field.getName(),
                        fieldType,
                        nestedSchema
                );
                objectSchema.addProperty(wrapper);
            } else {
                // Leaf field - use PropertyResolver with full metadata
                Property property = PropertyResolver.resolve(field);
                if (property != null) {
                    objectSchema.addProperty(property);
                }
            }
        }

        return objectSchema.isEmpty() ? new ObjectSchema() : objectSchema;
    }

    /**
     * Extracts the raw Class from the given type.
     */
    private static Class<?> extractClass(Type type) {
        if (type instanceof Class<?> cls) {
            return cls;
        }
        if (type instanceof ParameterizedType pt) {
            Type raw = pt.getRawType();
            if (raw instanceof Class<?> cls) {
                return cls;
            }
        }
        return null;
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
