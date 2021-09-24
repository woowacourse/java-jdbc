package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        log.debug("query : {}", sql);
        jdbcTemplate.queryDML(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "update users set account=?, password=?, email=? where id=?";
        log.debug("query : {}", sql);
        jdbcTemplate.queryDML(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id as users_id, account as users_account, " +
                "password as users_password, email as users_email " +
                "from users";
        log.debug("query : {}", sql);
        final List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        final Map<Object, List<Map<String, Object>>> resultByUser = result.stream()
                .collect(Collectors.groupingBy(it -> it.get("USERS_ID")));

        return resultByUser.values()
                .stream()
                .map(this::mapToUser)
                .collect(Collectors.toList());
    }

    public User findById(Long id) {
        final String sql = "select id as users_id, account as users_account, " +
                "password as users_password, email as users_email " +
                "from users " +
                "where id = ?";
        log.debug("query : {}", sql);
        final List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, id);

        return mapToUser(result);
    }


    public User findByAccount(String account) {
        final String sql = "select id as users_id, account as users_account, " +
                "password as users_password, email as users_email " +
                "from users " +
                "where account = ?";
        log.debug("query : {}", sql);
        final List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, account);

        return mapToUser(result);
    }

    public void deleteAll() {
        final String sql = "delete from users";
        log.debug("query : {}", sql);
        jdbcTemplate.queryDML(sql);
    }

    private User mapToUser(List<Map<String, Object>> maps) {
        if (maps.size() == 0) {
            return null;
        }

        return new User(
                (Long) maps.get(0).get("users_id"),
                (String) maps.get(0).get("users_account"),
                (String) maps.get(0).get("users_password"),
                (String) maps.get(0).get("users_email")
        );
    }
}
