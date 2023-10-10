package com.techcourse.service.user;

import com.techcourse.domain.User;
import java.sql.Connection;
import org.springframework.jdbc.datasource.DataSourceConfig;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionExecutor;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(AppUserService appUserService) {
        this.userService = appUserService;
    }

    @Override
    public User findById(long id) {
        DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        User user = TransactionExecutor.transactionQuery(() -> userService.findById(id));
        DataSourceUtils.releaseConnection(DataSourceConfig.getInstance());

        return user;
    }

    @Override
    public void insert(User user) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        TransactionExecutor.transactionCommand(connection, () -> userService.insert(user));
        DataSourceUtils.releaseConnection(DataSourceConfig.getInstance());
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        TransactionExecutor.transactionCommand(
                connection, () -> userService.changePassword(id, newPassword, createBy)
        );
        DataSourceUtils.releaseConnection(DataSourceConfig.getInstance());
    }

}
