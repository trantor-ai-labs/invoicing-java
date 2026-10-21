package com.acme.invoicing.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.PostConstruct;

public final class LifecycleRunner {
    private LifecycleRunner() {
    }

    public static <T> T initialise(T bean) {
        for (Method method : bean.getClass().getMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                invoke(bean, method);
            }
        }
        return bean;
    }

    private static void invoke(Object bean, Method method) {
        try {
            method.invoke(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot initialise " + bean.getClass().getSimpleName(), e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException("Initialiser failed", cause);
        }
    }
}
