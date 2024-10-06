package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.interface21.jdbc.querybuilder.ConditionExpression;
import com.interface21.jdbc.querybuilder.QueryBuilder;
import com.interface21.jdbc.querybuilder.query.Query;
import com.techcourse.dao.rowmapper.UserRowMapper;
import com.techcourse.domain.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> rowMapper = new UserRowMapper();

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        Query query = new QueryBuilder()
                .insert(List.of("account", "password", "email"))
                .from("users")
                .build();
        jdbcTemplate.queryForUpdate(query.getSql(), user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        Query query = new QueryBuilder()
                .update(List.of("account", "password", "email"))
                .from("users")
                .where(ConditionExpression.eq("id"))
                .build();

        jdbcTemplate.queryForUpdate(
                query.getSql(),
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public List<User> findAll() {
        Query query = new QueryBuilder()
                .selectFrom("users")
                .build();
        return jdbcTemplate.query(query.getSql(), rowMapper);
    }

    public User findById(final Long id) {
        Query query = resolveEqualSql("id");
        return jdbcTemplate.queryForObject(query.getSql(), rowMapper, id);
    }

    public User findByAccount(final String account) {
        Query query = resolveEqualSql("account");
        return jdbcTemplate.queryForObject(query.getSql(), rowMapper, account);
    }

    public Query resolveEqualSql(String fieldName) {
        return new QueryBuilder()
                .selectFrom("users")
                .where(ConditionExpression.eq(fieldName))
                .build();
    }
}
