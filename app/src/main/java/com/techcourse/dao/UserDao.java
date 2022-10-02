package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.DaoTemplateCallBack;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao extends DaoTemplateCallBack {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    public UserDao(final DataSource dataSource) {
        super(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        super.execute(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.executeUpdate();
        });
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        super.execute(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
            pstmt.executeUpdate();
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return super.queryAll(sql, (pstmt, rs) -> {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                final var user = new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
                users.add(user);
            }
            return users;
        });
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return super.queryOneLong(sql, id, (pstmt, rs) -> {
            if (rs.next()) {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            }
            return null;
        });
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return super.queryOneString(sql, account, (pstmt, rs) -> {
            if (rs.next()) {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            }
            return null;
        });
    }
}
