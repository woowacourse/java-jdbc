package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.exception.ExceptionExecutor;
import java.sql.Connection;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());

        ExceptionExecutor.run(
                () -> {
                    connection.setAutoCommit(false);
                    userService.changePassword(id, newPassword, createdBy);
                    connection.commit();
                },
                () -> ExceptionExecutor.run(connection::rollback),
                () -> ExceptionExecutor.run(() -> connection.setAutoCommit(true))
        );
    }
}
