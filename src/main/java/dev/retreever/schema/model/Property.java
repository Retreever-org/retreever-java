package dev.retreever.schema.model;

import dev.retreever.domain.model.JsonPropertyType;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a named field/property within a JSON object.
 */
public class Property implements Schema {

    private final String name;
    private final JsonPropertyType type;
    private final Schema value;

    private boolean required = false;
    private String description;
    private Object example;
    private final Set<String> constraints = new HashSet<>();

    public Property(String name, JsonPropertyType type, Schema value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    // --- Getters ---

    public String getName() {
        return name;
    }

    public JsonPropertyType getType() {
        return type;
    }

    public Schema getValue() {
        return value;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDescription() {
        return description;
    }

    public Object getExample() {
        return example;
    }

    public Set<String> getConstraints() {
        return constraints;
    }

    // --- Mutators (fluent) ---

    public Property required() {
        this.required = true;
        return this;
    }

    public Property description(String desc) {
        this.description = desc;
        return this;
    }

    public Property example(Object ex) {
        this.example = ex;
        return this;
    }

    public Property addConstraint(String constraint) {
        if (constraint != null && !constraint.isBlank()) {
            this.constraints.add(constraint);
        }
        return this;
    }
}
