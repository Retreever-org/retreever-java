package dev.retreever.engine;

import dev.retreever.config.SchemaConfig;
import dev.retreever.view.dto.ApiDocument;

import java.util.List;
import java.util.Set;

public class RetreeverOrchestrator {

//    private final ApiDocumentAssembler assembler;
//    private final SchemaResolver schemaResolver;
//    private final ApiErrorResolver errorResolver;
//    private final ApiDocResolver docResolver;
//
//    private final ApiErrorRegistry errorRegistry;

    public RetreeverOrchestrator(List<String> basePackages) {

        // 1. Initialise config
        SchemaConfig.init(basePackages);

//        // 2. Registries
//        this.errorRegistry = new ApiErrorRegistry();
//        ApiHeaderRegistry headerRegistry = new ApiHeaderRegistry();
//        SchemaRegistry schemaRegistry = new SchemaRegistry();
//
//        // 3. Resolver chain
//        ApiEndpointResolver endpointResolver =
//                new ApiEndpointResolver(headerRegistry);
//
//        ApiGroupResolver groupResolver =
//                new ApiGroupResolver(endpointResolver);
//
//        this.docResolver = new ApiDocResolver(groupResolver);
//
//        this.errorResolver = new ApiErrorResolver();
//
//        this.schemaResolver = new SchemaResolver(schemaRegistry);
//
//        this.assembler = new ApiDocumentAssembler(schemaRegistry, errorRegistry);
    }

    public ApiDocument build(Class<?> applicationClass,
                             Set<Class<?>> controllers,
                             Set<Class<?>> controllerAdvices) {

//        // 1) Resolve Errors BEFORE endpoints
//        List<Method> adviceMethods = controllerAdvices.stream()
//                .flatMap(c -> Stream.of(c.getDeclaredMethods()))
//                .collect(Collectors.toList());
//
//        List<ApiError> resolvedErrors = errorResolver.resolve(adviceMethods);
//
//        // FIX: ApiError MUST store (Type) errorBodyType, not schema.
//        resolvedErrors.forEach(errorRegistry::register);
//
//        // 2) Resolve ApiDoc (groups/endpoints/types only)
//        ApiDoc apiDoc = docResolver.resolve(applicationClass, controllers);
//
//        // 3) Pre-resolve ALL schema types into registry
//        preResolveAllSchemas(apiDoc);
//
//        // 4) Build final DTO
//        return assembler.assemble(apiDoc);

        return null;
    }

//    private void preResolveAllSchemas(ApiDoc doc) {
//
//        // Resolve endpoint schemas
//        doc.getGroups().forEach(group ->
//                group.getEndpoints().forEach(endpoint -> {
//
//                    // Request
//                    Type req = endpoint.getRequestBodyType();
//                    if (req != null) {
//                        schemaResolver.resolveRoot(req);
//                    }
//
//                    // Response
//                    Type resp = endpoint.getResponseBodyType();
//                    if (resp != null) {
//                        schemaResolver.resolveRoot(resp);
//                    }
//
//                    // Declared error types
//                    if (endpoint.getErrorBodyTypes() != null) {
//                        endpoint.getErrorBodyTypes()
//                                .forEach(schemaResolver::resolveRoot);
//                    }
//                })
//        );
//
//        // Resolve @ControllerAdvice errors
//        errorRegistry.values().forEach(err -> {
//            Type t = err.getErrorBodyType();
//            if (t != null) {
//                schemaResolver.resolveRoot(t);
//            }
//        });
//    }

}
