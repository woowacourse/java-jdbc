package com.techcourse.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class TransactionInvocationHandler implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(TransactionInvocationHandler.class);

    private final Object target;
    private final TransactionManager transactionManager;

    public TransactionInvocationHandler(final Object target, final TransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Method originalMethod = getOriginalMethod(method);

        if (originalMethod.isAnnotationPresent(Transactional.class)) {
            log.info("invoke: {}", method.getName());
            transactionManager.begin();
            try {
                final Object result = method.invoke(target, args);
                transactionManager.commit();
                return result;
            } catch (InvocationTargetException e) {
                transactionManager.rollback();
                throw e.getTargetException();
            }
        }

        return originalMethod.invoke(target, args);
    }

    private Method getOriginalMethod(final Method method) throws NoSuchMethodException {
        return target.getClass().getMethod(method.getName(), method.getParameterTypes());
    }
}
