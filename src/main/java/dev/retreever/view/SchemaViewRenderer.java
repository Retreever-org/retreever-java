package dev.retreever.view;

import dev.retreever.schema.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Renders Schema → 3-part JSON structure for API documentation.
 */
public final class SchemaViewRenderer {

    public static final String MODEL_KEY = "model";
    public static final String EXAMPLE_MODEL_KEY = "example_model";
    public static final String METADATA_KEY = "metadata";

    private static final Logger log = LoggerFactory.getLogger(SchemaViewRenderer.class);

    private SchemaViewRenderer() {}

    public static Map<String, Object> renderRequest(Schema schema) {
        return render(schema, true);
    }

    public static Map<String, Object> renderResponse(Schema schema) {
        return render(schema, false);
    }

    private static Map<String, Object> render(Schema schema, boolean includeMetadata) {
        if (schema == null) return null;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put(MODEL_KEY, renderModel(schema));
        result.put(EXAMPLE_MODEL_KEY, renderExample(schema));

        if (includeMetadata) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            buildMetadata(schema, "", metadata);
            result.put(METADATA_KEY, metadata);
        }
        return result;
    }

    /**
     * 🔥 FIXED: Property → getSchema() (full nested structure)
     */
    private static Object renderModel(Schema s) {
        if (s instanceof Property prop) {
            // ✅ CRITICAL: Use getSchema() not getType()
            return renderModel(prop.getValue());
        }
        if (s instanceof ValueSchema vs) {
            return vs.getType().displayName();
        }
        if (s instanceof ArraySchema arr) {
            Schema element = arr.getElementSchema();
            return element != null ? List.of(renderModel(element)) : List.of();
        }
        if (s instanceof ObjectSchema obj) {
            Map<String, Object> out = new LinkedHashMap<>();
            obj.getProperties().values().forEach(p ->
                    out.put(p.getName(), renderModel(p))  // Property → getSchema()
            );
            return out;
        }
        return null;
    }

    /**
     * 🔥 FIXED: Property → getSchema() for examples
     */
    private static Object renderExample(Schema s) {
        if (s instanceof Property prop) {
            // ✅ CRITICAL: Use getSchema() not leaf example
            return renderExample(prop.getValue());
        }
        if (s instanceof ArraySchema arr) {
            Schema element = arr.getElementSchema();
            return element != null ? List.of(renderExample(element)) : List.of();
        }
        if (s instanceof ObjectSchema obj) {
            Map<String, Object> out = new LinkedHashMap<>();
            obj.getProperties().values().forEach(p ->
                    out.put(p.getName(), renderExample(p))  // Property → getSchema()
            );
            return out;
        }
        return generateLeafExample(s);
    }

    /**
     * Leaf example generation (ValueSchema, primitives)
     */
    private static Object generateLeafExample(Schema s) {
        if (s instanceof ValueSchema vs) {
            return switch (vs.getType()) {
                case STRING -> "example";
                case NUMBER -> 123;
                case BOOLEAN -> true;
                case UUID -> "550e8400-e29b-41d4-a716-446655440000";
                case DATE_TIME -> "2025-01-29T10:15:30Z";
                case DATE -> "2025-01-29";
                default -> null;
            };
        }
        if (s instanceof Property prop) {
            return prop.getExample() != null ? prop.getExample() : "example";
        }
        return null;
    }

    private static void buildMetadata(Schema s, String parentPath, Map<String, Object> out) {
        if (s instanceof Property prop) {
            // ✅ FIXED: Handle Property metadata correctly
            if (prop.getValue() instanceof ValueSchema) {
                Map<String, Object> meta = new LinkedHashMap<>();
                meta.put("description", Optional.ofNullable(prop.getDescription()).orElse(""));
                meta.put("constraints", new ArrayList<>(prop.getConstraints()));
                out.put(parentPath.isEmpty() ? prop.getName() : parentPath, meta);
            } else {
                // Nested schema → recurse
                buildMetadata(prop.getValue(), parentPath, out);
            }
            return;
        }

        if (s instanceof ArraySchema arr) {
            Schema el = arr.getElementSchema();
            if (el != null) buildMetadata(el, parentPath + "[0]", out);
            return;
        }

        if (s instanceof ObjectSchema obj) {
            for (Property p : obj.getProperties().values()) {
                String path = parentPath.isEmpty() ? p.getName() : parentPath + "." + p.getName();
                buildMetadata(p, path, out);
            }
        }
    }
}
