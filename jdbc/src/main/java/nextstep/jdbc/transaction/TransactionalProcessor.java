package nextstep.jdbc.transaction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionalProcessor {

    private final PlatformTransactionManager manager;

    public TransactionalProcessor(DataSource dataSource) {
        this.manager = new DataSourceTransactionManager(dataSource);
    }

    private static class DeclarativeTransactionInterceptor implements MethodInterceptor {

        private final PlatformTransactionManager manager;
        private final List<Method> methodsToIntercept;

        public DeclarativeTransactionInterceptor(PlatformTransactionManager manager, List<Method> methodsToIntercept) {
            this.manager = manager;
            this.methodsToIntercept = methodsToIntercept;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            if (!methodsToIntercept.contains(method)) {
                return methodProxy.invokeSuper(o, objects);
            }
            TransactionStatus status = manager.getTransaction(new DefaultTransactionDefinition());
            try {
                Object invoked = methodProxy.invokeSuper(o, objects);
                manager.commit(status);
                return invoked;
            } catch (RuntimeException e) {
                manager.rollback(status);
                throw e;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> clazz, Object... arguments) {
        List<Method> methods = findMethodsToWrap(clazz);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new DeclarativeTransactionInterceptor(manager, methods));
        return (T) createProxyObject(enhancer, clazz, arguments);
    }

    private <T> List<Method> findMethodsToWrap(Class<T> clazz) {
        if (clazz.isAnnotationPresent(Transactional.class)) {
            return Arrays.stream(clazz.getMethods())
                    .collect(Collectors.toList());
        }
        return Arrays.stream(clazz.getMethods())
                .filter(method -> method.isAnnotationPresent(Transactional.class))
                .collect(Collectors.toList());
    }

    private Object createProxyObject(Enhancer enhancer, Class<?> clazz, Object... arguments) {
        Class<?>[] parameters = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> isAssignable(constructor, arguments))
                .findAny()
                .map(Constructor::getParameterTypes)
                .orElseThrow(() -> new IllegalArgumentException("fails to find appropriate constructor"));
        return enhancer.create(parameters, arguments);
    }

    private boolean isAssignable(Constructor<?> constructor, Object[] arguments) {
        Class<?>[] types = constructor.getParameterTypes();
        if (types.length != arguments.length) {
            return false;
        }
        return IntStream.range(0, arguments.length)
                .allMatch(index -> types[index].isAssignableFrom(arguments[index].getClass()));
    }
}
