package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        int rowCount = jdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail());
        log.debug("insert 성공한 row 개수 : {}", rowCount);
        return rowCount;
    }

    public int update(User user) {
        String sql = "update users set account=?, password=?, email=? where id=?";
        int rowCount = jdbcTemplate.executeUpdate(sql,
                user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        log.debug("update 성공한 row 개수 : {}", rowCount);
        return rowCount;
    }

    public List<User> findAll() {
        String sql = "select * from users";
        List<User> result = jdbcTemplate.query(sql, USER_ROW_MAPPER);
        log.debug("select 성공한 row 개수 : {}", result.size());
        return result;
    }

    public Optional<User> findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        Optional<User> result = jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
        result.ifPresentOrElse(
                user -> log.debug("select 성공한 row id : {}", user.getId()),
                () -> log.debug("다음 id에 해당하는 값이 존재하지 않습니다 : {}", id)
        );
        return result;
    }

    public Optional<User> findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";
        Optional<User> result = jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
        result.ifPresentOrElse(
                user -> log.debug("select 성공한 row id : {}", user.getId()),
                () -> log.debug("다음 account에 해당하는 값이 존재하지 않습니다 : {}", account)
        );
        return result;
    }
}
