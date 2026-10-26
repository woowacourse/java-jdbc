package com.techcourse.dao;

import com.interface21.RowMapper;
import com.interface21.jdbc.core.JdbcQueryTemplate;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    private final JdbcQueryTemplate queryTemplate;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.queryTemplate = new JdbcQueryTemplate(dataSource);
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
        };

        template.update();
    }

    public List<User> findAll() {
        String sql = """
                select id, account, password, email from users;
                """;
        return queryTemplate.query(sql, userRowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return queryTemplate.queryForObject(sql, userRowMapper, id);
    }

    public User findByAccount(final String account) {
        String sql = """
                select * from users where account = ?
                """;
        return queryTemplate.query(sql, userRowMapper, account).getFirst();
    }
}
