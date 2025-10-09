package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    DataSource getDataSource() {
        return dataSource;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        final PreparedStatementSetter pss = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        };
        final JdbcTemplate insertJdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };

        insertJdbcTemplate.update(sql, pss);
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        final PreparedStatementSetter pss = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        };
        final JdbcTemplate updateJdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };

        updateJdbcTemplate.update(sql, pss);
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        final RowMapper rowMapper = rs -> {
            final List<User> users = new ArrayList<>();
            while (rs.next()) {
                final User user = new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                );
                users.add(user);
            }
            return users;
        };
        final PreparedStatementSetter pss = pstmt -> {
        };
        final JdbcTemplate selectJdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };

        return (List<User>) selectJdbcTemplate.query(sql, pss, rowMapper);
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        final RowMapper rowMapper = rs -> {
            if (rs.next()) {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                );
            }
            return null;
        };
        final PreparedStatementSetter pss = pstmt -> {
            pstmt.setLong(1, id);
        };
        final JdbcTemplate selectJdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };

        return (User) selectJdbcTemplate.query(sql, pss, rowMapper);
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        final RowMapper rowMapper = rs -> {
            if (rs.next()) {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                );
            }
            return null;
        };
        final PreparedStatementSetter pss = pstmt -> {
            pstmt.setString(1, account);
        };
        final JdbcTemplate selectJdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };

        return (User) selectJdbcTemplate.query(sql, pss, rowMapper);
    }
}
