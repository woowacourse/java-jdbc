package aop.stage1;

import java.lang.reflect.Method;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import aop.Transactional;

/**
 * 포인트컷(pointcut). 어드바이스를 적용할 조인 포인트를 선별하는 클래스.
 * TransactionPointcut 클래스는 메서드를 대상으로 조인 포인트를 찾는다.
 * <p>
 * 조인 포인트(join point). 어드바이스가 적용될 위치
 * <p>
 * StaticMethodMatcherPointcut: 해당 클래스를 상속 받아 정적인 메서드 매칭을 위해 matches를 구현한다.
 * StaticMethodMatcherPointcut.matches: 어드바이스가 특정 메서드에 적용될지를 판단한다.
 */
public class TransactionPointcut extends StaticMethodMatcherPointcut {

    @Override
    public boolean matches(final Method method, final Class<?> targetClass) {
        return method.isAnnotationPresent(Transactional.class); // 어드바이스 적용 대상 여부 판단 기준
    }
}
