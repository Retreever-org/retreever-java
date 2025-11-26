package dev.retreever.schema.resolver;

import dev.retreever.domain.model.JsonProperty;
import dev.retreever.domain.model.JsonPropertyType;
import dev.retreever.schema.context.ResolvedTypeContext;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * The central dispatcher.
 * Decides how a field should be resolved:
 *
 * OBJECT → JsonObjectResolver
 * ARRAY  → JsonArrayResolver
 * SIMPLE → JsonSimplePropResolver
 * * FIX: Implements recursion detection using TypeResolver helpers to prevent StackOverflowError.
 */
public class JsonPropertyResolver {

    public static JsonProperty resolve(Field field, Type actualType, ResolvedTypeContext ctx) {

        // 1. Resolve generics
        Type resolved = ctx.resolve(actualType);

        // --- RECURSION GUARD START ---
        if (TypeResolver.isTypeResolving(ctx, resolved)) {
            String refName = TypeResolver.resolveRefName(resolved);
            // Return a reference property, breaking the cycle
            return JsonProperty.reference(refName).description("Reference to recursive type: " + refName);
        }

        TypeResolver.markTypeResolving(ctx, resolved);
        // --- RECURSION GUARD END ---

        JsonProperty prop = null;
        try {

            Class<?> raw = TypeResolver.extractRawClass(resolved);

            // 2. Classify type and create base property
            JsonPropertyType type = JsonPropertyTypeResolver.resolve(raw);
            prop = JsonProperty.of(field.getName(), type);

            // 3. Dispatch and delegate to specialized resolvers
            switch (type) {

                case OBJECT -> {
                    JsonObjectResolver obj = new JsonObjectResolver(ctx);
                    prop.addObjectProperty(obj.resolve(resolved));
                }

                case ARRAY -> {
                    JsonArrayResolver arr = new JsonArrayResolver(ctx);
                    // CRITICAL FIX: The ArrayResolver returns the full ARRAY property.
                    prop = arr.resolve(resolved, field.getName());
                    break;
                }

                default -> {
                    // This handles all SIMPLE types AND adds metadata to the OBJECT case.
                    JsonSimplePropResolver.resolve(prop, field);
                }
            }

            return prop;

        } finally {
            // Unmark the type after its resolution is complete.
            TypeResolver.unmarkTypeResolving(ctx, resolved);
        }
    }

    public static JsonProperty resolveElement(Type type, ResolvedTypeContext ctx, String elementName) {

        // 1. Resolve generics
        Type resolved = ctx.resolve(type);

        // --- RECURSION GUARD START ---
        if (TypeResolver.isTypeResolving(ctx, resolved)) {
            String refName = TypeResolver.resolveRefName(resolved);
            // Return a reference property, breaking the cycle
            return JsonProperty.reference(refName).description("Reference to recursive type: " + refName);
        }

        TypeResolver.markTypeResolving(ctx, resolved);
        // --- RECURSION GUARD END ---

        JsonProperty prop = null;
        try {

            Class<?> raw = TypeResolver.extractRawClass(resolved);

            // 2. Classify type and create base property
            JsonPropertyType jsonType = JsonPropertyTypeResolver.resolve(raw);
            prop = JsonProperty.of(elementName, jsonType);

            // 3. Dispatch and delegate
            switch (jsonType) {

                case OBJECT -> {
                    JsonObjectResolver obj = new JsonObjectResolver(ctx);
                    prop.addObjectProperty(obj.resolve(resolved));
                }

                case ARRAY -> {
                    JsonArrayResolver arr = new JsonArrayResolver(ctx);
                    // CRITICAL FIX: The ArrayResolver returns the full ARRAY property.
                    prop = arr.resolve(resolved, elementName);
                    break;
                }

                default -> {
                    // No action for SIMPLE types, as there is no field metadata to apply
                }
            }

            return prop;

        } finally {
            // Unmark the type after its resolution is complete.
            TypeResolver.unmarkTypeResolving(ctx, resolved);
        }
    }
}