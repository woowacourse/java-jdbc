package com.techcourse.dao;

import com.interface21.jdbc.bind.RowMapper;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.NamedJdbcTemplate;
import com.interface21.jdbc.core.NamedSqlParamMap;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final NamedJdbcTemplate namedJdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.namedJdbcTemplate = new NamedJdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.namedJdbcTemplate = new NamedJdbcTemplate(jdbcTemplate);
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (:account, :password, :email)";

        log.debug("insert user: {}", user);
        NamedSqlParamMap params = getSqlParamMapForInsert(user);

        namedJdbcTemplate.update(sql, params);
    }

    private NamedSqlParamMap getSqlParamMapForInsert(User user) {
        NamedSqlParamMap params = new NamedSqlParamMap()
            .addValue("account", user.getAccount())
            .addValue("password", user.getPassword())
            .addValue("email", user.getEmail());
        return params;
    }

    public void update(final User user) {
        final var sql = "update users set account = :account, password = :password, email = :email where id = :id";

        log.info("update user: {}", user);
        NamedSqlParamMap params = getSqlParamMapForUpdate(user);

        namedJdbcTemplate.update(sql, params);
    }

    private NamedSqlParamMap getSqlParamMapForUpdate(User user) {
        NamedSqlParamMap params = new NamedSqlParamMap()
            .addValue("account", user.getAccount())
            .addValue("email", user.getEmail())
            .addValue("id", user.getId())
            .addValue("password", user.getPassword());
        return params;
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return namedJdbcTemplate.select(sql, userRowMapper());
    }

    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = :id";

        NamedSqlParamMap param = new NamedSqlParamMap("id", id);
        User user = namedJdbcTemplate.selectForOne(sql, param, userRowMapper());

        return Optional.ofNullable(user);
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = :account";

        NamedSqlParamMap param = new NamedSqlParamMap("account", account);
        User user = namedJdbcTemplate.selectForOne(sql, param, userRowMapper());

        return Optional.ofNullable(user);
    }

    private RowMapper<User> userRowMapper() {
        return rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
        );
    }
}
