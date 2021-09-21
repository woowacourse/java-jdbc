package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.PreparedStatementSetter;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {

        String query = "insert into users (account, password, email) values (?, ?, ?)";

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        jdbcTemplate.update(query, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }
        });
    }

    public void update(User user) {

        String query = "update users set account=?, password=?, email=? where id=?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        jdbcTemplate.update(query, new PreparedStatementSetter() {
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

        String query = "select id, account, password, email from users";

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        return (List<User>) jdbcTemplate.query(query, new PreparedStatementSetter() {
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
        String query = "select id, account, password, email from users where id = ?";

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        return (User) jdbcTemplate.query(query, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }
        }, new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                if(rs.next()) {
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
        String query = "select id, account, password, email from users where account = ?";

        JdbcTemplate jdbcTemplate = new JdbcTemplate() {

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        return (User) jdbcTemplate.query(query, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }
        }, new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs) throws SQLException {
                if(rs.next()) {
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
