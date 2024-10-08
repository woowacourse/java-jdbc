package com.techcourse.dao;

import com.interface21.jdbc.core.AbstractJdbcTemplate;
import com.interface21.jdbc.core.PrepareStatementSetter;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;

public class JdbcUserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private static final PrepareStatementSetter STATEMENT_SETTER = (preparedStatement, objects) -> {
        if (objects == null) {
            return;
        }
        for (int index = 0; index < objects.length; index++) {
            preparedStatement.setObject(index + 1, objects[index]);
        }
    };

    private final DataSource dataSource;

    public JdbcUserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(final User user) {
        AbstractJdbcTemplate jdbcTemplate = new AbstractJdbcTemplate(dataSource) {

            @Override
            protected String createQuery() {
                return "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        jdbcTemplate.update(STATEMENT_SETTER, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        AbstractJdbcTemplate jdbcTemplate = new AbstractJdbcTemplate(dataSource) {

            @Override
            protected String createQuery() {
                return "UPDATE users SET account=?, password=?, email=? WHERE id=?";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        jdbcTemplate.update(STATEMENT_SETTER, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        AbstractJdbcTemplate jbcTemplate = new AbstractJdbcTemplate(dataSource) {
            @Override
            protected String createQuery() {
                return "SELECT id, account, password, email FROM users";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        return jbcTemplate.queryList(USER_ROW_MAPPER, STATEMENT_SETTER);
    }

    public User findById(final Long id) {
        AbstractJdbcTemplate jbcTemplate = new AbstractJdbcTemplate(dataSource) {
            @Override
            protected String createQuery() {
                return "SELECT id, account, password, email FROM users WHERE id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        return jbcTemplate.query(USER_ROW_MAPPER, STATEMENT_SETTER, id);
    }

    public User findByAccount(final String account) {
        AbstractJdbcTemplate jbcTemplate = new AbstractJdbcTemplate(dataSource) {
            @Override
            protected String createQuery() {
                return "SELECT id, account, password, email FROM users WHERE account=?";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        return jbcTemplate.query(USER_ROW_MAPPER, STATEMENT_SETTER, account);
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
