package aop.stage2;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;

public class CustomProxyBeanProcessor implements BeanPostProcessor {
    private final Advisor advisor;

    public CustomProxyBeanProcessor(Advisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Service.class)) {
            ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
            proxyFactoryBean.setTarget(bean);
            proxyFactoryBean.setProxyTargetClass(true);

            proxyFactoryBean.addAdvisor(advisor);
            return proxyFactoryBean.getObject();
        }
        return bean;

//        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
//        proxyFactoryBean.setTarget(bean);
//        proxyFactoryBean.setProxyTargetClass(true);
//
//        proxyFactoryBean.addAdvisor(advisor);
//        return proxyFactoryBean.getObject();
    }
}
