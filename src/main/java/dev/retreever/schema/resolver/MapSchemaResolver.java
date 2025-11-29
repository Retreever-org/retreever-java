package dev.retreever.schema.resolver;

import dev.retreever.schema.context.ResolverContext;
import dev.retreever.schema.model.ObjectSchema;
import dev.retreever.schema.model.Property;
import dev.retreever.schema.model.Schema;
import dev.retreever.schema.model.JsonPropertyType;
import dev.retreever.schema.resolver.util.TypeUtils;

import java.lang.reflect.Type;

/**
 * Resolves Map<String, V> into an {@link ObjectSchema}.
 * JSON requires object keys to be strings, so only String-keyed maps
 * are supported directly. Others degrade into {"string": null}.
 */
public final class MapSchemaResolver {

    private MapSchemaResolver() {}

    public static Schema resolve(Type type, ResolverContext ctx) {

        Type keyType = TypeUtils.getMapKeyType(type);
        Type valueType = TypeUtils.getMapValueType(type);

        ObjectSchema schema = new ObjectSchema();

        boolean keyIsString = TypeUtils.getRawClass(keyType) == String.class;

        // Key MUST be String, otherwise degrade to {"string": null}
        if (!keyIsString) {
            schema.getProperties().put(
                    "string",
                    new Property("string", JsonPropertyType.STRING, null)
            );
            return schema;
        }

        // Recursion guard → {"string": {}}
        if (ctx.registry().isResolving(valueType)) {
            schema.getProperties().put(
                    "string",
                    new Property("string", JsonPropertyType.OBJECT, null)
            );
            return schema;
        }

        // Normal resolution of value schema
        Schema valueSchema = ctx.orchestrator().resolve(valueType, ctx);

        // Add synthetic property for "string" key
        schema.getProperties().put(
                "string",
                new Property("string", JsonPropertyType.OBJECT, valueSchema)
        );

        return schema;
    }
}
