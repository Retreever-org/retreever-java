package dev.retreever.engine;

import dev.retreever.config.SchemaConfig;
import dev.retreever.doc.resolver.ApiDocResolver;
import dev.retreever.endpoint.resolver.ApiEndpointResolver;
import dev.retreever.endpoint.resolver.ApiErrorResolver;
import dev.retreever.group.resolver.ApiGroupResolver;
import dev.retreever.repo.ApiErrorRegistry;
import dev.retreever.repo.ApiHeaderRegistry;
import dev.retreever.repo.SchemaRegistry;
import dev.retreever.view.ApiDocumentAssembler;
import dev.retreever.view.dto.ApiDocument;

import java.util.List;
import java.util.Set;

public class RetreeverOrchestrator {

    private final ApiDocumentAssembler assembler;
    private final ApiErrorResolver errorResolver;
    private final ApiDocResolver docResolver;

    private final ApiErrorRegistry errorRegistry;

    public RetreeverOrchestrator(List<String> basePackages) {

        // 1. Initialise config
        SchemaConfig.init(basePackages);

        // 2. Registries
        this.errorRegistry = new ApiErrorRegistry();
        ApiHeaderRegistry headerRegistry = new ApiHeaderRegistry();
        SchemaRegistry schemaRegistry = SchemaRegistry.getInstance();

        // 3. Resolver chain
        ApiEndpointResolver endpointResolver =
                new ApiEndpointResolver(headerRegistry);

        ApiGroupResolver groupResolver =
                new ApiGroupResolver(endpointResolver);

        this.docResolver = new ApiDocResolver(groupResolver);

        this.errorResolver = new ApiErrorResolver();

        this.assembler = new ApiDocumentAssembler(schemaRegistry, errorRegistry);
    }

    public ApiDocument build(Class<?> applicationClass,
                             Set<Class<?>> controllers,
                             Set<Class<?>> controllerAdvices) {

        return null;
    }

}
