package com.interface21.transaction.handler;

import com.interface21.transaction.Transactional;
import com.interface21.transaction.support.Transaction;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransactionHandler implements InvocationHandler {

    private final Transaction transaction;
    private final Object target;

    public TransactionHandler(Transaction transaction, Object target) {
        this.transaction = transaction;
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (targetMethod.isAnnotationPresent(Transactional.class)) {
            return invokeWithTransaction(method, args);
        }
        return invokeWithoutTransaction(method, args);
    }

    private Object invokeWithTransaction(Method method, Object[] args) throws Throwable {
        transaction.begin();
        try {
            final Object invoke = method.invoke(target, args);
            transaction.commit();
            return invoke;
        } catch (InvocationTargetException e) {
            transaction.rollback();
            throw e.getTargetException();
        } catch (Throwable e) {
            transaction.rollback();
            throw e;
        }
    }

    private Object invokeWithoutTransaction(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
