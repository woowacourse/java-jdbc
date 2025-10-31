package com.techcourse.service;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TransactionUserService.class);

    private final UserService userService;
    private final DataSource dataSource;

    public TransactionUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                log.error("트랜잭션 롤백 실패", ex);
            }
            throw new RuntimeException("비밀번호 변경 중 오류 발생", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close(); // 풀에 반납하고
                }
            } catch (SQLException e) {
                log.error("커넥션 닫기 실패", e);
            }
            // 커넥션 제거 해줘야함
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }
}
