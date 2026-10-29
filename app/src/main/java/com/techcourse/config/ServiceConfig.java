package com.techcourse.config;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.aop.TransactionProxyFactory;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.service.UserService;

import javax.sql.DataSource;

// 서비스는 이곳에서만 생성해 @Transactional 프록시가 항상 적용되도록 한다.
public class ServiceConfig {

    // 클래스 초기화 시점에 한 번만 생성되고, JVM이 클래스 초기화를 동기화해 주므로 동시에 호출해도 하나만 만들어진다.
    private static final UserService USER_SERVICE = createUserService();

    public static UserService userService() {
        return USER_SERVICE;
    }

    public static UserService userService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        return TransactionProxyFactory.createProxy(
                new UserService(userDao, userHistoryDao),
                DataSourceConfig.getInstance()
        );
    }

    private static UserService createUserService() {
        final DataSource dataSource = DataSourceConfig.getInstance();
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return userService(new UserDao(jdbcTemplate), new UserHistoryDao(jdbcTemplate));
    }

    private ServiceConfig() {}
}
