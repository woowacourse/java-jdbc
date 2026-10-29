package com.techcourse.config;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.aop.TransactionProxyFactory;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.service.UserService;

import javax.sql.DataSource;

// 서비스는 이곳에서만 생성해 @Transactional 프록시가 항상 적용되도록 한다.
public class ServiceConfig {

    public static UserService userService() {
        return Holder.INSTANCE;
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

    // Holder 클래스는 처음 접근될 때 한 번만 초기화되고, JVM이 클래스 초기화를 동기화해 주므로 프록시가 하나만 만들어진다.
    private static class Holder {
        private static final UserService INSTANCE = createUserService();
    }

    private ServiceConfig() {}
}
