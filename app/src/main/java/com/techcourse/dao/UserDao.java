package com.techcourse.dao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        PreparedCallBack callBack = (connection) -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.executeUpdate();
            pstmt.close();
        };

        commandTemplate(sql, callBack);
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        PreparedCallBack callBack = (connection) -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
            pstmt.executeUpdate();
            pstmt.close();
        };

        commandTemplate(sql, callBack);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users;";

        return query(sql, (rs) -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4))
        );
    }

    private <T> List<T> query(String sql, ResultSetCallBack<T> callBack, Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<T> results = new ArrayList<>();
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            int index = 1;
            for (Object arg : args) {
                pstmt.setObject(index++, arg);
            }
            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);

            while (rs.next()) {
                T result = callBack.callback(rs);
                results.add(result);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }

        return results;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return queryOne(sql, (rs) -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4)), id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return queryOne(sql, (rs) -> new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)),
                account
        );
    }

    private <T> T queryOne(String sql, ResultSetCallBack<T> callBack, Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = this.dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            int index = 1;
            for (Object arg : args) {
                pstmt.setObject(index++, arg);
            }
            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);

            if (rs.next()) {
                return callBack.callback(rs);
            }

            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    // delete, uddate, insert
    // 인자가 필요한 버전, 인자가 필요하지 않은 버전
    private void commandTemplate(String sql, PreparedCallBack callBack) {
        log.debug("query : {}", sql);
        try (Connection connection = dataSource.getConnection()) {
            callBack.callback(connection);
            // executeUpdate를 공통으로 사용해야함

            // 예외 처리
        } catch (Exception ingnore) {
        }
    }

    private interface PreparedCallBack {


        void callback(Connection connection) throws SQLException;
    }
    private interface ResultSetCallBack<T> {


        T callback(ResultSet resultSet) throws SQLException;
    }
}
