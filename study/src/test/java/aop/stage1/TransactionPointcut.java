package aop.stage1;

import aop.Transactional;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

/**
 * 포인트컷(pointcut). 어드바이스를 적용할 조인 포인트를 선별하는 클래스.
 * TransactionPointcut 클래스는 메서드를 대상으로 조인 포인트를 찾는다.
 * <p>
 * 조인 포인트(join point). 어드바이스가 적용될 위치
 */
public class TransactionPointcut extends StaticMethodMatcherPointcut {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (hasTransactionalAnnotation(method, targetClass)) {
            return true;
        }

        Method specificMethod = getSpecificMethod(method, targetClass);
        return hasTransactionalAnnotation(specificMethod, specificMethod.getDeclaringClass());
    }

    private boolean hasTransactionalAnnotation(Method method, Class<?> targetClass) {
        return method.isAnnotationPresent(Transactional.class) || targetClass.isAnnotationPresent(Transactional.class);
    }

    // Method를 상속받거나 인터페이스로부터 오버라이드된 메서드 찾기
    private Method getSpecificMethod(Method method, Class<?> targetClass) {
        try {
            return targetClass.getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return method;
        }
    }
}
