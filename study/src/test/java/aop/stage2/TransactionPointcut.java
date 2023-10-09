package aop.stage2;

import aop.Transactional;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

public class TransactionPointcut extends StaticMethodMatcherPointcut {

    @Override
    public boolean matches(final Method method, final Class<?> targetClass) {
        return targetClass.isAnnotationPresent(Service.class) &&
                method.isAnnotationPresent(Transactional.class);
    }
}
