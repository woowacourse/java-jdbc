package com.techcourse.dao;

import com.interface21.RowMapper;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private static final RowMapper<User> userRowMapper = (rs, row) -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate.getDataSource());
    }

    public void insert(final User user) {
        JdbcTemplate template = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            public DataSource getDataSource() {
                return UserDao.this.dataSource;
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }

            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                return null;
            }
        };

        template.update();
    }

    public void update(final User user) {
        JdbcTemplate template = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "update users set account = ?, password = ?, email = ? where id = ?";
            }

            @Override
            public DataSource getDataSource() {
                return UserDao.this.dataSource;
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            }

            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                return null;
            }
        };

        template.update();
    }

    public List<User> findAll() {
        JdbcTemplate template = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            public DataSource getDataSource() {
                return UserDao.this.dataSource;
            }

            @Override
            protected void setValues(PreparedStatement pstmt) {
            }

            @Override
            protected List<User> mapRow(ResultSet rs) throws SQLException {
                return mapUsers(rs);
            }
        };

        return (List<User>) template.query();
    }

    public User findById(final Long id) {
        JdbcTemplate template = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            public DataSource getDataSource() {
                return UserDao.this.dataSource;
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setObject(1, id);
            }

            @Override
            protected User mapRow(ResultSet rs) throws SQLException {
                List<User> users = mapUsers(rs);
                if (users.size() != 1) {
                    throw new IllegalStateException(
                            "조회 결과는 정확히 1건이어야 합니다. 실제: " + users.size());
                }
                return users.getFirst();
            }
        };

        return (User) template.query();
    }

    public User findByAccount(final String account) {
        JdbcTemplate template = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select * from users where account = ?";
            }

            @Override
            public DataSource getDataSource() {
                return UserDao.this.dataSource;
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }

            @Override
            protected User mapRow(ResultSet rs) throws SQLException {
                return mapUsers(rs).getFirst();
            }
        };

        return (User) template.query();
    }

    private List<User> mapUsers(final ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            users.add(userRowMapper.mapRow(rs, rs.getRow()));
        }
        return users;
    }
}
