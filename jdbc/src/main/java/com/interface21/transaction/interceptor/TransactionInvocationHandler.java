package com.interface21.transaction.interceptor;

import com.interface21.transaction.annotation.Transactional;
import com.interface21.transaction.support.TransactionTemplate;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class TransactionInvocationHandler implements InvocationHandler {

    private final Object target;
    private final TransactionTemplate transactionTemplate;

    TransactionInvocationHandler(final Object target, final TransactionTemplate transactionTemplate) {
        this.target = target;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "Transactional proxy for " + target;
                default -> throw new IllegalStateException("Unsupported Object method: " + method);
            };
        }
        if (!isTransactional(method)) {
            return invokeTarget(method, args);
        }

        try {
            return transactionTemplate.execute(() -> invokeUnchecked(method, args));
        } catch (CheckedInvocationException e) {
            final var cause = e.getCause();
            for (final var suppressed : e.getSuppressed()) {
                if (suppressed != cause) {
                    cause.addSuppressed(suppressed);
                }
            }
            throw cause;
        }
    }

    private boolean isTransactional(final Method method) throws NoSuchMethodException {
        final var targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        return method.isAnnotationPresent(Transactional.class)
                || targetMethod.isAnnotationPresent(Transactional.class);
    }

    private Object invokeUnchecked(final Method method, final Object[] args) {
        try {
            return invokeTarget(method, args);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new CheckedInvocationException(e);
        }
    }

    private Object invokeTarget(final Method method, final Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static final class CheckedInvocationException extends RuntimeException {

        private CheckedInvocationException(final Throwable cause) {
            super(cause);
        }
    }
}
