package com.techcourse.config;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.aop.TransactionProxyFactory;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.service.UserService;

import javax.sql.DataSource;
import java.util.Objects;

// 서비스는 이곳에서만 생성해 @Transactional 프록시가 항상 적용되도록 한다.
public class ServiceConfig {

    private static UserService USER_SERVICE;

    public static UserService userService() {
        if (Objects.isNull(USER_SERVICE)) {
            final DataSource dataSource = DataSourceConfig.getInstance();
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            USER_SERVICE = userService(new UserDao(jdbcTemplate), new UserHistoryDao(jdbcTemplate));
        }
        return USER_SERVICE;
    }

    public static UserService userService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        return TransactionProxyFactory.createProxy(
                new UserService(userDao, userHistoryDao),
                DataSourceConfig.getInstance()
        );
    }

    private ServiceConfig() {}
}
