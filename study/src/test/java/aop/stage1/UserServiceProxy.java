package aop.stage1;

import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

public class UserServiceProxy {

    private final UserService target;

    public UserServiceProxy(UserDao userDao, UserHistoryDao userHistoryDao,
                            PlatformTransactionManager transactionManager) {
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();

        UserService userService = new UserService(userDao, userHistoryDao);
        proxyFactoryBean.setTarget(userService);

        TransactionAdvisor advisor = new TransactionAdvisor(transactionManager);
        proxyFactoryBean.addAdvisor(advisor);

        this.target = (UserService) proxyFactoryBean.getObject();
    }

    public UserService getTarget() {
        return this.target;
    }
}
