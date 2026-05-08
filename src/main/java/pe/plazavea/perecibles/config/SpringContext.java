package pe.plazavea.perecibles.config;

import org.springframework.context.ApplicationContext;

public final class SpringContext {

    private static ApplicationContext context;

    private SpringContext() {
    }

    public static void init(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        if (context == null) {
            throw new IllegalStateException("SpringContext has not been initialized.");
        }
        return context.getBean(clazz);
    }
}
