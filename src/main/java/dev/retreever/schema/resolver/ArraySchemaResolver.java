package dev.retreever.schema.resolver;

import dev.retreever.schema.context.ResolverContext;
import dev.retreever.schema.model.ArraySchema;
import dev.retreever.schema.model.Schema;
import dev.retreever.schema.resolver.util.TypeUtils;

import java.lang.reflect.Type;

/**
 * Resolves List<T>, Set<T>, Stream<T>, and T[] into {@link ArraySchema}.
 */
public final class ArraySchemaResolver {

    private ArraySchemaResolver() {}

    /**
     * Resolves an array-like structure into {@link ArraySchema}.
     */
    public static Schema resolve(Type type, ResolverContext ctx) {

        Type elementType = TypeUtils.getArrayElementType(type);

        // If recursion guard is already triggered for this element type:
        if (ctx.registry().isResolving(elementType)) {
            // Recursion placeholder: ArraySchema with no inner schema.
            return new ArraySchema(null);
        }

        // Resolve element schema normally via orchestrator.
        Schema elementSchema = ctx.orchestrator().resolve(elementType, ctx);

        return new ArraySchema(elementSchema);
    }
}
