package aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class JdkDynamicAopProxy {

    private Class<?> interfaces;
    private InvocationHandler invocationHandler;

    public void addAdvice(InvocationHandler invocationHandler) {
        this.invocationHandler = invocationHandler;
    }

    public void setInterfaces(Class<?> interfaces) {
        this.interfaces = interfaces;
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(
                JdkDynamicAopProxy.class.getClassLoader(),
                new Class<?>[]{interfaces},
                invocationHandler);
    }
}
