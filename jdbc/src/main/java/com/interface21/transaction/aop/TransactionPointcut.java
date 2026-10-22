package com.interface21.transaction.aop;

import com.interface21.transaction.annotation.Transactional;
import java.lang.reflect.Method;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

public class TransactionPointcut extends StaticMethodMatcherPointcut {

    @Override
    public boolean matches(final Method method, final Class<?> targetClass) {
        final Method targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        return targetMethod.isAnnotationPresent(Transactional.class);
    }
}
