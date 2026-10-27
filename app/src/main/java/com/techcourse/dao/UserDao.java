package com.techcourse.dao;

import com.interface21.RowMapper;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private static final RowMapper<User> userRowMapper = (rs, row) -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate<?> jdbcTemplate) {
        this(jdbcTemplate.getDataSource());
    }

    public void insert(final User user) {
        JdbcTemplate<Void> template = new JdbcTemplate<>() {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            public DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        template.update(pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        JdbcTemplate<Void> template = new JdbcTemplate<>() {
            @Override
            protected String createQuery() {
                return "update users set account = ?, password = ?, email = ? where id = ?";
            }

            @Override
            public DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        template.update(pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        });
    }

    public List<User> findAll() {
        JdbcTemplate<User> template = new JdbcTemplate<>() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            public DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        return template.query(userRowMapper);
    }

    public User findById(final Long id) {
        JdbcTemplate<User> template = new JdbcTemplate<>() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            public DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        List<User> users = template.query(userRowMapper, pstmt -> pstmt.setObject(1, id));
        if (users.size() != 1) {
            throw new IllegalStateException(
                    "조회 결과는 정확히 1건이어야 합니다. 실제: " + users.size());
        }
        return users.getFirst();
    }

    public User findByAccount(final String account) {
        JdbcTemplate<User> template = new JdbcTemplate<>() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            public DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        return template.query(userRowMapper, pstmt -> pstmt.setString(1, account)).getFirst();
    }
}
