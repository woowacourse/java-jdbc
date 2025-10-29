package com.techcourse.service;

import javax.sql.DataSource;

import com.interface21.transaction.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        final DataSource dataSource = DataSourceConfig.getInstance();
        try {
            TransactionManager.executeInTransaction(dataSource,
                () -> userService.changePassword(id, newPassword, createdBy)
            );
        } catch (Exception e) {
            throw new BusinessException("비밀번호 변경에 실패했습니다.", e);
        }
    }
}
