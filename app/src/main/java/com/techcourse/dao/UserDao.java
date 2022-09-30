package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.PreparedStatementSetter;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, ps -> {
            ps.setString(1, user.getAccount());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        // todo
    }

    public List<User> findAll() {
        // todo
        final var sql = "select id, account, password, email from users";
        final var result = new ArrayList<User>();

        try (final var conn = dataSource.getConnection();
             var pstmt = conn.prepareStatement(sql)) {

            try (final var rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new User(rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)));
                }
            }
            return result;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("account"),
                rs.getString("password"), rs.getString("email"));
        final PreparedStatementSetter pss = ps -> ps.setLong(1, id);
        return jdbcTemplate.queryForObject(sql, userRowMapper, pss)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다 id = " + id));
    }

    public User findByAccount(final String account) {
        // todo
        return null;
    }
}
