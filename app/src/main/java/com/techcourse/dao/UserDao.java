package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        JdbcTemplate insertJdbcTemplate = new JdbcTemplate() {
            @Override
            DataSource getDataSource() {
                return dataSource;
            }
        };

        insertJdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }
        });
    }

    public void update(User user) {
        final String sql = "update users set account=?, password=?, email=? where id=?";
        JdbcTemplate updateJdbcTemplate = new JdbcTemplate() {
            @Override
            DataSource getDataSource() {
                return dataSource;
            }
        };

        updateJdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            }
        });
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        JdbcTemplate selectAllJdbcTemplate = new JdbcTemplate() {
            @Override
            DataSource getDataSource() {
                return dataSource;
            }
        };

        return (List<User>) selectAllJdbcTemplate.query(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
            }
        }, new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                List<User> result = new ArrayList<>();

                while(rs.next()) {
                    result.add(new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)));
                }
                return result;
            }
        });
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        JdbcTemplate selectByIdJdbcTemplate = new JdbcTemplate() {
            @Override
            DataSource getDataSource() {
                return dataSource;
            }
        };

        return (User) selectByIdJdbcTemplate.query(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }
        }, new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4));
                }
                return null;
            }
        });
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        JdbcTemplate selectByIdJdbcTemplate = new JdbcTemplate() {
            @Override
            DataSource getDataSource() {
                return dataSource;
            }
        };

        return (User) selectByIdJdbcTemplate.query(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }
        }, new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4));
                }
                return null;
            }
        });
    }
}
