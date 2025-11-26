package dev.retreever.schema.context;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public record ResolvedParamType(
        Class<?> raw,
        Type[] args
) implements ParameterizedType {

    @Override
    public Type[] getActualTypeArguments() {
        return args;
    }

    @Override
    public Type getRawType() {
        return raw;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}

