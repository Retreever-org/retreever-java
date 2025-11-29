package dev.retreever.view;


import dev.retreever.schema.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Renders a Schema tree into the final 3-part documentation structure:
 *
 * <pre>
 * {
 *   "model": { ... },
 *   "example_model": { ... },
 *   "metadata": { ... }   // only for request bodies
 * }
 * </pre>
 *
 * This renderer is intentionally stateless and pure.
 */
public final class SchemaViewRenderer {

    private static final Logger log = LoggerFactory.getLogger(SchemaViewRenderer.class);

    private SchemaViewRenderer() {}

    // PUBLIC API

    public static Map<String, Object> renderRequest(Schema schema) {
        if (isSchemaNull(schema)) return null;
        return render(schema, true);
    }

    public static Map<String, Object> renderResponse(Schema schema) {
        if (isSchemaNull(schema)) return null;
        return render(schema, false);
    }

    // INTERNAL

    private static boolean isSchemaNull(Schema schema) {
        if(schema == null) {
            log.error("Schema is null");
            return true;
        }
        return false;
    }

    private static Map<String, Object> render(Schema schema, boolean includeMetadata) {

        Map<String, Object> root = new LinkedHashMap<>();

        Object model = renderModel(schema);
        Object example = renderExample(schema);

        root.put("model", model);
        root.put("example_model", example);

        if (includeMetadata) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            buildMetadata(schema, "", metadata);
            root.put("metadata", metadata);
        }

        return root;
    }

    // MODEL RENDERING

    private static Object renderModel(Schema s) {

        if (s instanceof ValueSchema vs) {
            return vs.getType().displayName();
        }

        if (s instanceof ArraySchema arr) {
            Schema element = arr.getElementSchema();
            if (element == null) return List.of();
            return List.of(renderModel(element));
        }

        if (s instanceof ObjectSchema obj) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Property p : obj.getProperties().values()) {
                out.put(p.getName(), renderModel(p.getValue()));
            }
            return out;
        }

        return null;
    }

    // EXAMPLE RENDERING

    private static Object renderExample(Schema s) {

        if (s instanceof ValueSchema) {
            return null; // primitive example not available at schema-level
        }

        if (s instanceof ArraySchema arr) {
            Schema element = arr.getElementSchema();
            if (element == null) return List.of();
            return List.of(renderExample(element));
        }

        if (s instanceof ObjectSchema obj) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Property p : obj.getProperties().values()) {
                out.put(p.getName(), renderExample(p.getValue()));
            }
            return out;
        }

        return null;
    }

    // METADATA RENDERING (REQUEST ONLY)

    private static void buildMetadata(
            Schema s,
            String parentPath,
            Map<String, Object> out
    ) {

        if (s instanceof ValueSchema) {
            // ValueSchema only collected when wrapped inside Property
            return;
        }

        if (s instanceof ArraySchema arr) {
            Schema el = arr.getElementSchema();
            if (el != null) {
                String path = parentPath.isEmpty() ? "[0]" : parentPath + "[]";
                buildMetadata(el, path, out);
            }
            return;
        }

        if (s instanceof ObjectSchema obj) {
            for (Property p : obj.getProperties().values()) {

                String path = parentPath.isEmpty()
                        ? p.getName()
                        : parentPath + "." + p.getName();

                Schema value = p.getValue();

                if (value instanceof ValueSchema) {
                    Map<String, Object> meta = new LinkedHashMap<>();
                    meta.put("description", Optional.ofNullable(p.getDescription()).orElse(""));
                    meta.put("constraints", p.getConstraints());
                    out.put(path, meta);
                } else {
                    buildMetadata(value, path, out);
                }
            }
        }
    }
}
