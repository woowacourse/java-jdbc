package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    private final RowMapper<User> rowMapper = (resultSet) -> {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
        );
    };

    public void insert(final User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = jdbcTemplate.getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);
            List<User> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public Optional<User> findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";

        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
    }

    public Optional<User> findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";

        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, account));
    }
}
