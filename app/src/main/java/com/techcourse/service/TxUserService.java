package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(AppUserService appUserService) {
        this.userService = appUserService;
    }

    @Override
    public User findById(long id) {
        return executeWithTransaction(() -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        executeWithTransaction(() -> {
                    userService.insert(user);
                    return null;
                }
        );
    }

    // override 대상인 메서드는 userService의 메서드를 그대로 위임(delegate)한다.
    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        // 트랜잭션 처리 영역
        executeWithTransaction(() -> {
                    userService.changePassword(id, newPassword, createBy);
                    return null;
                }
        );
        // 트랜잭션 처리 영역
    }

    private <T> T executeWithTransaction(final ServiceCallBack<T> serviceCallBack) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            T result = serviceCallBack.execute();
            connection.commit();
            return result;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

}
