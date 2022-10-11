package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import javax.sql.DataSource;
import nextstep.jdbc.TransactionTemplateManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

public class TxUserService implements UserService {

    private final TransactionTemplateManager transactionManager;
    private final UserService userService;

    // override 대상인 메서드는 userService의 메서드를 그대로 위임(delegate)한다.
    public TxUserService(final TransactionTemplateManager transactionManager, final UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return transactionManager.doTransaction(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionManager.doTransaction(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.doTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }
}
