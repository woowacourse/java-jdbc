package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        Connection connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        Connection connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        Connection connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        String sql = "select * from users";

        return jdbcTemplate.query(connection, sql, USER_ROW_MAPPER);
    }

    public User findById(Long id) {
        Connection connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        String sql = "select * from users where id = ?";

        return jdbcTemplate.queryForObject(connection, sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(String account) {
        Connection connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        String sql = "select * from users where account = ?";

        return jdbcTemplate.queryForObject(connection, sql, USER_ROW_MAPPER, account);
    }
}
