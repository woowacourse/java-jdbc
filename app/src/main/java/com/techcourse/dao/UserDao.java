package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import nextstep.jdbc.SimpleJdbcInsert;
import nextstep.web.annotation.Repository;

@Repository
public class UserDao {

    private static final RowMapper<User> MAPPER = (rs) -> new User(
        rs.getLong("id"),
        rs.getString("account"),
        rs.getString("password"),
        rs.getString("email")
    );

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource, "users", "id");
    }

    public User insert(User user) {
        Map<String, String> parameters = new HashMap<>();

        parameters.put("account", user.getAccount());
        parameters.put("password", user.getPassword());
        parameters.put("email", user.getEmail());

        Long id = (Long) jdbcInsert.executeAndReturnKey(parameters);
        return User.generateId(id, user);
    }

    public List<User> findAll() {
        final String sql = "select * from users";

        return jdbcTemplate.query(sql, MAPPER);
    }

    public Optional<User> findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, MAPPER, id);
    }

    public Optional<User> findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, MAPPER, account);
    }

    public int update(User user) {
        final String sql = "update users set account=?, password=?, email=? where id=?";

        return jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public int deleteAll() {
        final String sql = "delete from users";

        return jdbcTemplate.update(sql);
    }
}
