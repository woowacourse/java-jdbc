package com.interface21.transaction.aop;

import javax.sql.DataSource;
import org.springframework.aop.framework.ProxyFactory;

public final class TransactionProxyFactory {

    private TransactionProxyFactory() {}

    // @Transactional이 붙은 메서드를 호출하면 트랜잭션 안에서 실행되도록 target을 프록시로 감싼다.
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(final T target, final DataSource dataSource) {
        final ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvisor(new TransactionAdvisor(
                new TransactionPointcut(),
                new TransactionAdvice(dataSource)
        ));
        return (T) proxyFactory.getProxy();
    }
}
