package dev.retreever.schema.resolver;

import dev.retreever.repo.SchemaRegistry;
import dev.retreever.schema.context.PropertyTypeContext;
import dev.retreever.schema.context.ResolverContext;
import dev.retreever.schema.model.*;
import dev.retreever.schema.resolver.util.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.Temporal;
import java.util.UUID;

/**
 * Central orchestrator responsible for converting Java {@link Type}s into
 * Retreever {@link Schema} models and storing them in {@link SchemaRegistry}.
 *
 * <p>This resolver performs:
 * <ul>
 *     <li>Unwrapping of ResponseEntity&lt;T&gt; and Optional&lt;T&gt;</li>
 *     <li>Generic type-variable resolution via {@link PropertyTypeContext}</li>
 *     <li>Dispatching to concrete schema resolvers</li>
 *     <li>Cycle detection + schema caching</li>
 * </ul>
 *
 * <p>No schema is returned to the caller. All resolved schema objects are stored
 * directly in {@link SchemaRegistry}. Other modules obtain schemas from the
 * registry using the resolved Type keys.</p>
 */
public final class SchemaResolver {
    private final Logger log = LoggerFactory.getLogger(SchemaResolver.class);
    private final SchemaRegistry registry;

    public SchemaResolver(SchemaRegistry registry) {
        this.registry = registry;
    }

    /**
     * Resolves a top-level Java type into a schema and registers it.
     * This method SHOULD be called for:
     * <ul>
     *     <li>request body types</li>
     *     <li>response body types</li>
     *     <li>ApiError body types</li>
     * </ul>
     *
     * <p>A new {@link PropertyTypeContext} is created per root to bind generic
     * type variables correctly (T, U, etc.).</p>
     *
     * @param rootType original user-declared type
     */
    public void resolveRoot(Type rootType) {
        PropertyTypeContext typeContext = new PropertyTypeContext(rootType);
        ResolverContext ctx = new ResolverContext(typeContext, registry, this);

        // unwrap + generics
        Type resolvedType = typeContext.resolve(unwrap(rootType));

        // fully resolve schema and write to registry
        resolve(resolvedType, ctx);
    }

    /**
     * Internal recursive resolver responsible for producing the correct
     * Schema implementation and storing it in the registry if applicable.
     *
     * @param type Java reflective type (after generic substitution)
     * @param ctx  shared resolution context
     * @return resolved Schema instance
     */
    public Schema resolve(Type type, ResolverContext ctx) {

        // 1. unwrap wrappers (Optional<T>, ResponseEntity<T>)
        type = unwrap(type);

        // 2. resolve T → concrete type
        type = ctx.typeContext().resolve(type);

        Class<?> raw = TypeUtils.getRawClass(type);

        // 3. Cycle detection (recursion)
        if (ctx.registry().isResolving(type)) {
            return recursionPlaceholder(raw);
        }

        // 4. Cache lookup (POJOs only)
        if (ctx.registry().hasResolved(type)) {
            return ctx.registry().getResolved(type);
        }

        // 5. Mark as resolving
        ctx.registry().markResolving(type);

        Schema result;

        // 6. Dispatch to specialized resolvers
        if (isValueType(raw)) {
            result = ValueSchemaResolver.resolve(type, ctx);
        }
        else if (isMapType(type)) {
            result = MapSchemaResolver.resolve(type, ctx);
        }
        else if (isArrayType(type)) {
            result = ArraySchemaResolver.resolve(type, ctx);
        }
        else if (isPojo(raw)) {
            result = ObjectSchemaResolver.resolve(type, ctx);
        }
        else {
            result = ValueSchemaResolver.resolve(type, ctx);
        }

        // 7. Mark resolved
        ctx.registry().unmarkResolving(type);

        // 8. Cache reusable POJOs only
        ctx.registry().saveResolved(type, result);

        return result;
    }


    // Helpers

    private Type unwrap(Type type) {
        if (TypeUtils.isResponseEntity(type)) {
            return TypeUtils.unwrapResponseEntity(type);
        }
        if (TypeUtils.isOptional(type)) {
            return TypeUtils.unwrapOptional(type);
        }
        return type;
    }

    private boolean isValueType(Class<?> clazz) {
        return clazz.isPrimitive()
                || Number.class.isAssignableFrom(clazz)
                || CharSequence.class.isAssignableFrom(clazz)
                || clazz == Boolean.class
                || clazz == UUID.class
                || clazz.isEnum()
                || Temporal.class.isAssignableFrom(clazz)
                || clazz == java.net.URI.class
                || clazz == Duration.class
                || clazz == Period.class;
    }

    private boolean isArrayType(Type type) {
        return TypeUtils.isArray(type)
                || TypeUtils.isCollection(type)
                || TypeUtils.isStream(type);
    }

    private boolean isMapType(Type type) {
        return TypeUtils.isMap(type);
    }

    private boolean isPojo(Class<?> clazz) {
        String pkg = clazz.getPackageName();
        return !clazz.isPrimitive()
                && !clazz.isEnum()
                && !clazz.isArray()
                && !pkg.startsWith("java.")
                && !pkg.startsWith("javax.")
                && !pkg.startsWith("jakarta.");
    }

    /**
     * Returned when a cycle is detected: A → B → A.
     * Produces minimal but type-correct placeholders.
     */
    private Schema recursionPlaceholder(Class<?> raw) {

        if (raw.isArray()
                || java.util.Collection.class.isAssignableFrom(raw)
                || java.util.stream.Stream.class.isAssignableFrom(raw)) {
            return new ArraySchema(null);
        }

        if (java.util.Map.class.isAssignableFrom(raw)) {
            ObjectSchema s = new ObjectSchema();
            s.getProperties().put(
                    "string",
                    new Property("string", JsonPropertyType.OBJECT, null)
            );
            return s;
        }

        return new ObjectSchema();
    }
}
