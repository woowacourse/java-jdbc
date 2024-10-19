package aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class JdkDynamicAopProxy {

    private Class<?> interfaces;
    private InvocationHandler invocationHandler;

    public void addAdvice(final InvocationHandler invocationHandler) {
        this.invocationHandler = invocationHandler;
    }

    public void setInterfaces(final Class<?> interfaces) {
        this.interfaces = interfaces;
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(
                JdkDynamicAopProxy.class.getClassLoader(),
                new Class<?>[]{interfaces},
                invocationHandler);
    }
}
