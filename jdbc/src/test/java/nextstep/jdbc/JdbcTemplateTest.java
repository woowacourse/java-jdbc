package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JdbcTemplateTest {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplateTest.class);

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private User user;

    @BeforeEach
    void setup() {
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);

        user = new User(1L, "account", "password", "email@email.com");
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    @AfterEach
    void cleanUp() {
        final String sql = "drop table users";
        jdbcTemplate.update(sql);
    }

    @Test
    void insert() {
        final User newUser = new User("newAccount", "newPassword", "newEmail@email.com");
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, newUser.getAccount(), newUser.getPassword(), newUser.getEmail());

        User dbUser = findUserByIdUsingJdbc(2L);
        assertThat(dbUser.getAccount()).isEqualTo(newUser.getAccount());
        assertThat(dbUser.getPassword()).isEqualTo(newUser.getPassword());
        assertThat(dbUser.getEmail()).isEqualTo(newUser.getEmail());
    }

    @Test
    void update() {
        final String sql = "update users set password = ? where id = ?";
        final String newPassword = "newPassword";

        jdbcTemplate.update(sql, newPassword, user.getId());

        User dbUser = findUserByIdUsingJdbc(1L);
        assertThat(dbUser.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void findById() {
        final String sql = "select id, account, password, email from users where id = ?";
        final Long id = 1L;

        final User dbUser = jdbcTemplate.queryObject(sql, User.class, id);

        assertThat(dbUser.getId()).isEqualTo(user.getId());
        assertThat(dbUser.getAccount()).isEqualTo(user.getAccount());
        assertThat(dbUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(dbUser.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void findByAccount() {
        final String sql = "select id, account, password, email from users where account = ?";
        final String account = "account";

        final User dbUser = jdbcTemplate.queryObject(sql, User.class, account);

        assertThat(dbUser.getId()).isEqualTo(user.getId());
        assertThat(dbUser.getAccount()).isEqualTo(user.getAccount());
        assertThat(dbUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(dbUser.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void findAll() {
        List<User> users = new ArrayList<>();
        users.add(user);
        final User user2 = new User("account2", "password2", "email2@email.com");
        users.add(user2);
        final User user3 = new User("account3", "password3", "email3@email.com");
        users.add(user3);
        final String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, user2.getAccount(), user2.getPassword(), user2.getEmail());
        jdbcTemplate.update(insertSql, user3.getAccount(), user3.getPassword(), user3.getEmail());
        final String sql = "select id, account, password, email from users";

        List<User> dbUsers = jdbcTemplate.query(sql, User.class);

        assertThat(dbUsers.size()).isEqualTo(users.size());
    }


    private User findUserByIdUsingJdbc(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            if (rs.next()) {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            }

            return null;
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
}
