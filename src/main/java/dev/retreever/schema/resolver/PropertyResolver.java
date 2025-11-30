/*
 * Copyright (c) 2025 Retreever Contributors
 *
 * Licensed under the MIT License.
 * You may obtain a copy of the License at:
 *     [https://opensource.org/licenses/MIT](https://opensource.org/licenses/MIT)
 */

package dev.retreever.schema.resolver;

import dev.retreever.schema.model.JsonPropertyType;
import dev.retreever.schema.model.Property;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Resolves Field instances into fully enriched Property instances,
 * including constraints, description, and example metadata.
 * <p>
 * Contract: Only non-object, non-array field types (leaves).
 */
public class PropertyResolver {

    /**
     * Resolves the given Field into a fully enriched {@link Property}.
     *
     * @param field the Field to resolve (must NOT be array/object type)
     * @return fully enriched Property instance, or null if field is null
     * @throws IllegalArgumentException if field type is array or object
     */
    public static Property resolve(Field field) {
        if (field == null) {
            return null;
        }

        Type fieldType = field.getGenericType();
        Class<?> rawType = field.getType();
        JsonPropertyType propType = JsonPropertyTypeResolver.resolve(rawType);

        if (propType == JsonPropertyType.ARRAY || propType == JsonPropertyType.OBJECT) {
            throw new IllegalArgumentException(
                    "PropertyResolver cannot resolve ARRAY or OBJECT field types: " +
                            field.getName() + " (" + rawType.getSimpleName() + ")"
            );
        }

        Property property = new Property(field.getName(), propType, null);

        // Enrich with metadata using existing resolvers
        PropertyConstraintResolver.resolve(property, field);
        PropertyDescriptionResolver.resolve(property, field);
        PropertyExampleResolver.resolve(property, field);

        return property;
    }
}
