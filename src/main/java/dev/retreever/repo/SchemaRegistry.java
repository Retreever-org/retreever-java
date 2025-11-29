package dev.retreever.repo;

import dev.retreever.schema.model.Schema;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class SchemaRegistry {
    private static final Map<String, Schema> schemas = new HashMap<>();

    private SchemaRegistry() {
        // Initialize the registry with default schemas if needed
    }

    public void register(Type type, Schema schema){
        if(schemas.containsKey(type.getTypeName())) return;
        schemas.put(type.getTypeName(), schema);
    }

    public Schema getSchema(Type type){
        return schemas.get(type.getTypeName());
    }
}
