package com.techcourse.service;

import javax.sql.DataSource;
import com.interface21.transaction.support.JdbcTransactionManager;
import com.interface21.transaction.support.JdbcTransactionTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final JdbcTransactionTemplate jdbcTransactionTemplate;
    private final UserService targetUserService;

    public TxUserService(UserService targetUserService) {
        this.targetUserService = targetUserService;
        this.jdbcTransactionTemplate = createTransactionTemplate();
    }

    private JdbcTransactionTemplate createTransactionTemplate() {
        DataSource dataSource = DataSourceConfig.getInstance();
        return new JdbcTransactionTemplate(new JdbcTransactionManager(dataSource));
    }

    @Override
    public User findById(long id) {
        return targetUserService.findById(id);
    }

    @Override
    public void insert(User user) {
        targetUserService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        jdbcTransactionTemplate.execute((transaction -> {
            targetUserService.changePassword(id, newPassword, createBy);
        }));
    }
}
