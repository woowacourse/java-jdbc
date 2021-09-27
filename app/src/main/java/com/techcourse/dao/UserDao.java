package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

public class UserDao { // todo: 일단은 이 코드를 다 동작하게

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

//    public UserDao(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
//    }

    public void insert(User user) { // todo: SimpleJdbcInsert 도 구현해보자!!
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

//    public List<User> findAll() { // todo: query(...) 로 추상화
//        final String sql = "select id, account, password, email from users";
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql);
//             ResultSet rs = pstmt.executeQuery()) {
//
//            List<User> users = new ArrayList<>();
//            while (rs.next()) {
//                User user = parseUser(rs);
//                users.add(user);
//            }
//            return users;
//        } catch (SQLException e) {
//            //todo: Custom Exception
//            throw new IllegalArgumentException();
//        }
//    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, mapperUser());


//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql);
//             ResultSet rs = pstmt.executeQuery()) {
//
//            List<User> users = new ArrayList<>();
//            while (rs.next()) {
//                User user = parseUser(rs);
//                users.add(user);
//            }
//            return users;
//        } catch (SQLException e) {
//            //todo: Custom Exception
//            throw new IllegalArgumentException();
//        }
    }


    public User findById(Long id) { // todo: queryForObject(...)로 추상화
        final String sql = "select id, account, password, email from users where id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = createPreParedStatementByIndex(conn, sql, id);
             ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);

            if (rs.next()) {
                return parseUser(rs);
            }

            // todo: 데이터가 존재하지 않는 경우 예외 추가
            throw new IllegalArgumentException("해당 데이터가 존재하지 않습니다.");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement createPreParedStatementByIndex(Connection conn, String sql, Long id) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, id);
        return pstmt;
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = ((Supplier<PreparedStatement>) () -> {
                 try {
                     PreparedStatement s = conn.prepareStatement(sql);
                     s.setString(1, account);
                     return s;
                 } catch (SQLException e) {
                     throw new RuntimeException(e);
                 }
             }).get();
             ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);

            if (rs.next()) {
                return parseUser(rs);
            }

            // todo: 데이터가 존재하지 않는 경우 예외 추가
            throw new IllegalArgumentException("해당 데이터가 존재하지 않습니다.");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private User parseUser(ResultSet rs) throws SQLException {
        return new User(rs.getLong(1), rs.getString(2), rs.getString(3),
                rs.getString(4));
    }

    private RowMapper<User> mapperUser() {
        return (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("account"),
                rs.getString("password"), rs.getString("email"));
    }
}
