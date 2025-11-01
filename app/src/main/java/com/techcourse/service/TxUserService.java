package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final DataSource dataSource = DataSourceConfig.getInstance();
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
        try (Connection conn = DataSourceUtils.getConnection(dataSource)) {
            try {
                conn.setAutoCommit(false);
                userService.changePassword(id, newPassword, createdBy);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new IllegalStateException("비밀번호 변경 중 오류가 발생했습니다.");
            }

        } catch (SQLException e) {
            throw new IllegalStateException("데이터베이스 연결에 실패했습니다.");
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
