package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);
    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.dataSource = DataSourceConfig.getInstance();
    }

    @Override
    public User findById(final long id) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        boolean previous = false;

        try {
            previous = conn.isReadOnly();
            conn.setReadOnly(true);

            return userService.findById(id);
        } catch (SQLException e) {
            throw new DataAccessException("유저 정보 조회 실패", e);
        } finally {
            try {
                conn.setReadOnly(previous);
            } catch (SQLException e) {
                log.warn("Read Only 실패", e);
            }
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    @Override
    public void save(final User user) {
        executeInTransaction(
                () -> userService.save(user),
                "User 정보 저장 실패"
        );
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeInTransaction(
                () -> userService.changePassword(id, newPassword, createBy),
                "비밀번호 변경 실패"
        );
    }

    private void executeInTransaction(Runnable work, String errorMessage) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        boolean mustReset = false;
        try {
            if (conn.getAutoCommit()) {
                conn.setAutoCommit(false);
                mustReset = true;
            }

            try {
                work.run();
                processCommit(mustReset, conn);
            } catch (Exception e) {
                processRollback(mustReset, conn, e);
                throw e;
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException(errorMessage, e);
        } finally {
            processAutoCommit(mustReset, conn);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void processCommit(boolean mustReset, Connection conn) {
        if (mustReset) {
            try {
                conn.commit();
            } catch (SQLException e) {
                throw new DataAccessException("commit 실패", e);
            }
        }
    }

    private void processRollback(boolean mustReset, Connection conn, Exception originalExceptionMessage) {
        if (mustReset) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                originalExceptionMessage.addSuppressed(e);
            }
        }
    }

    private void processAutoCommit(boolean mustReset, Connection conn) {
        if (mustReset) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                log.warn("auto commit 복원 실패", e);
            }
        }
    }
}
