package dev.retreever.schema.context;

import dev.retreever.repo.SchemaRegistry;

public record ResolverContext(
        PropertyTypeContext typeContext,
        SchemaRegistry registry,
        SchemaResolver orchestrator
) {}
