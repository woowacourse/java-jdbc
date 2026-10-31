package com.techcourse.service;

import com.interface21.transaction.TransactionTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import javax.sql.DataSource;

/**
 * 감싼 UserService의 메서드 하나를 트랜잭션 하나로 실행한다.
 */
public class TxUserService implements UserService {

    private final TransactionTemplate transactionTemplate;
    private final UserService userService;

    public TxUserService(final UserService userService) {
        this(DataSourceConfig.getInstance(), userService);
    }

    public TxUserService(final DataSource dataSource, final UserService userService) {
        this.transactionTemplate = new TransactionTemplate(dataSource);
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return transactionTemplate.execute(() -> userService.findById(id));
    }

    @Override
    public void insert(final User user) {
        transactionTemplate.executeWithoutResult(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.executeWithoutResult(() -> userService.changePassword(id, newPassword, createBy));
    }
}
