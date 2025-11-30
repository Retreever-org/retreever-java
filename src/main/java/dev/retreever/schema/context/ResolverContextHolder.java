package dev.retreever.schema.context;

public final class ResolverContextHolder {
    private static final ThreadLocal<ResolverContext> CONTEXT =
            ThreadLocal.withInitial(() -> null);

    public static void init(ResolverContext context) {
        CONTEXT.set(context);
    }

    public static ResolverContext get() {
        return CONTEXT.get();
    }

    public static ResolverContext getOrEmpty() {
        ResolverContext ctx = CONTEXT.get();
        return ctx != null ? ctx : new ResolverContext();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
