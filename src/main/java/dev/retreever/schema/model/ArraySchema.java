package dev.retreever.schema.model;

/**
 * Represents a JSON array.
 * The array holds a schema describing its element shape.
 */
public class ArraySchema implements Schema {

    private final Schema elementSchema;

    public ArraySchema(Schema elementSchema) {
        this.elementSchema = elementSchema;
    }

    public Schema getElementSchema() {
        return elementSchema;
    }
}
