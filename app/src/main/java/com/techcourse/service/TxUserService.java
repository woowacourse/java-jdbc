package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.JdbcUserHistoryDao;
import com.techcourse.domain.User;
import org.springframework.jdbc.transaction.TransactionManager;

import static com.techcourse.config.DataSourceConfig.getInstance;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(UserService userService) {
        this.userService = userService;
        this.transactionManager = new TransactionManager(getInstance());
    }

    @Override
    public User findById(final long id) {
        return transactionManager.execute(connection -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionManager.execute(connection -> {
                    userService.insert(user);
                    return null;
                }
        );
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.execute(connection -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }

}
