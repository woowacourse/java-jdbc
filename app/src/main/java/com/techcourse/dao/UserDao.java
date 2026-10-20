package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final AbstractJdbcTemplate template = new AbstractJdbcTemplate(jdbcTemplate.getDataSource()) {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }
        };
        template.update(pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final AbstractJdbcTemplate template = new AbstractJdbcTemplate(jdbcTemplate.getDataSource()) {
            @Override
            protected String createQuery() {
                return "update users set account = ?, password = ?, email = ? where id = ?";
            }
        };
        final int updatedRows = template.update(pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        });
        if (updatedRows == 0) {
            throw new DataAccessException("수정할 사용자가 존재하지 않습니다. id = " + user.getId());
        }
    }

    public List<User> findAll() {
        final AbstractJdbcTemplate template = new AbstractJdbcTemplate(jdbcTemplate.getDataSource()) {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users";
            }
        };
        return template.query(USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final AbstractJdbcTemplate template = new AbstractJdbcTemplate(jdbcTemplate.getDataSource()) {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }
        };
        return template.queryForObject(USER_ROW_MAPPER, pstmt -> pstmt.setLong(1, id));
    }

    public User findByAccount(final String account) {
        final AbstractJdbcTemplate template = new AbstractJdbcTemplate(jdbcTemplate.getDataSource()) {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }
        };
        return template.queryForObject(USER_ROW_MAPPER, pstmt -> pstmt.setString(1, account));
    }
}
