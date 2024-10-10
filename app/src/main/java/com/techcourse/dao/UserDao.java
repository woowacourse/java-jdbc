package com.techcourse.dao;

import javax.sql.DataSource;
import java.util.List;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementCallBack;
import com.interface21.jdbc.core.ResultSetCallBack;
import com.techcourse.domain.User;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final ResultSetCallBack<User> resultSetCallBack;

    public UserDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetCallBack = getUserResultSetCallBack();
    }

    private ResultSetCallBack<User> getUserResultSetCallBack() {
        return (rs) -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4)
        );
    }

    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        PreparedStatementCallBack callBack = (pstmt) -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        };

        jdbcTemplate.update(sql, callBack);
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        PreparedStatementCallBack callBack = (pstmt) -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        };

        jdbcTemplate.update(sql, callBack);
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users;";

        return jdbcTemplate.query(sql, resultSetCallBack);
    }

    public User findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryOne(sql, resultSetCallBack, id);
    }

    public User findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryOne(sql, resultSetCallBack, account);
    }
}
