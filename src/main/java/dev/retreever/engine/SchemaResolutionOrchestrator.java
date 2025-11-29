/*
 * Copyright (c) 2025 Retreever Contributors
 *
 * Licensed under the MIT License.
 * You may obtain a copy of the License at:
 *     [https://opensource.org/licenses/MIT](https://opensource.org/licenses/MIT)
 */

package dev.retreever.engine;

import dev.retreever.config.SchemaConfig;
import dev.retreever.repo.SchemaRegistry;
import dev.retreever.schema.model.Schema;
import dev.retreever.schema.resolver.SchemaResolver;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;

/**
 * Orchestrates complete schema resolution for REST controllers and exception handlers.
 * Extracts endpoints/parameters, unwraps containers, resolves schemas, and registers them.
 */
public class SchemaResolutionOrchestrator {

    private final SchemaRegistry schemaRegistry;

    public SchemaResolutionOrchestrator(SchemaRegistry schemaRegistry) {
        this.schemaRegistry = schemaRegistry;
    }

    public void resolveAllSchema(Class<?> applicationClass,
                                 Set<Class<?>> controllers,
                                 Set<Class<?>> controllerAdvices) {

        // Process REST Controllers
        processControllers(controllers);

        // Process Exception Handlers
        processControllerAdvices(controllerAdvices);

        // Optimize registry
        schemaRegistry.optimize();
    }

    private void processControllers(Set<Class<?>> controllers) {
        for (Class<?> controller : controllers) {
            if (!isBasePackageClass(controller)) continue;

            for (Method method : controller.getDeclaredMethods()) {
                if (!isRestEndpoint(method)) continue;

                // Register return type schema
                processReturnType(method.getGenericReturnType());

                // Register @RequestBody/@ModelAttribute parameter schemas
                processMethodParameters(method);
            }
        }
    }

    private void processControllerAdvices(Set<Class<?>> controllerAdvices) {
        for (Class<?> advice : controllerAdvices) {
            if (!isBasePackageClass(advice)) continue;

            for (Method method : advice.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(ExceptionHandler.class)) continue;

                // Register exception handler return type
                processReturnType(method.getGenericReturnType());

                // Register request body parameters (if any)
                processMethodParameters(method);
            }
        }
    }

    private void processReturnType(Type returnType) {
        Type unwrappedType = unwrapContainerType(returnType);
        registerSchemaIfValid(unwrappedType);
    }

    private void processMethodParameters(Method method) {
        Parameter[] parameters = method.getParameters();
        for (Parameter param : parameters) {
            if (isRequestBodyOrModelAttribute(param)) {
                Type unwrappedType = unwrapContainerType(param.getParameterizedType());
                registerSchemaIfValid(unwrappedType);
            }
        }
    }

    // === TYPE PROCESSING ===

    private Type unwrapContainerType(Type type) {
        Class<?> rawType = SchemaResolver.extractRawClass(type);

        // ResponseEntity<T>
        if (rawType == ResponseEntity.class && type instanceof ParameterizedType pt) {
            return pt.getActualTypeArguments()[0];
        }

        // Optional<T>
        if (rawType == Optional.class && type instanceof ParameterizedType pt) {
            return pt.getActualTypeArguments()[0];
        }

        return type;
    }

    private void registerSchemaIfValid(Type type) {
        Class<?> rawClass = SchemaResolver.extractRawClass(type);
        if (rawClass != null && !rawClass.isPrimitive() && !rawClass.isEnum()
                && isBasePackageClass(rawClass)) {

            Schema schema = SchemaResolver.resolve(type);
            schemaRegistry.register(type, schema);
        }
    }

    // === FILTERING ===

    private boolean isBasePackageClass(Class<?> clazz) {
        if (clazz == null) return false;
        String packageName = clazz.getPackageName();
        return SchemaConfig.getBasePackages().stream()
                .anyMatch(packageName::startsWith);
    }

    // === ENDPOINT DETECTION ===

    private boolean isRestEndpoint(Method method) {
        return method.isAnnotationPresent(RequestMapping.class) ||
                method.isAnnotationPresent(GetMapping.class) ||
                method.isAnnotationPresent(PostMapping.class) ||
                method.isAnnotationPresent(PutMapping.class) ||
                method.isAnnotationPresent(DeleteMapping.class) ||
                method.isAnnotationPresent(PatchMapping.class);
    }

    private boolean isRequestBodyOrModelAttribute(Parameter param) {
        return param.isAnnotationPresent(RequestBody.class) ||
                param.isAnnotationPresent(ModelAttribute.class);
    }
}
