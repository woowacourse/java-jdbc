package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import java.sql.Connection;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionUserService extends TransactionService<UserService> implements UserService {

    public TransactionUserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        super(new AppUserService(userDao, userHistoryDao));
    }

    public User findById(final long id) {
        final Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        final User user = appService.findById(id);
        DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
        return user;
    }

    public void insert(final User user) {
        TransactionTemplate.executeWithoutReturn(() -> appService.insert(user), DataSourceConfig.getInstance());
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionTemplate.executeWithoutReturn(
                () -> appService.changePassword(id, newPassword, createBy), DataSourceConfig.getInstance()
        );
    }
}
