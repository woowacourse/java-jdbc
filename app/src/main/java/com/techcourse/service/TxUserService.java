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
        Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            conn.setAutoCommit(false);

            try {
                userService.save(user);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("User 정보 저장 실패", e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);

            try {
                userService.changePassword(id, newPassword, createBy);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("비밀번호 변경 실패", e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
