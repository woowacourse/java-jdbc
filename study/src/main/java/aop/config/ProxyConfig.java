package aop.config;

import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import aop.service.AppUserService;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyConfig {

    @Bean
    public ProxyFactoryBean userServiceProxy(UserDao userDao, UserHistoryDao userHistoryDao) {
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(new AppUserService(userDao, userHistoryDao));
        proxyFactoryBean.setProxyTargetClass(true);
        return proxyFactoryBean;
    }
}
