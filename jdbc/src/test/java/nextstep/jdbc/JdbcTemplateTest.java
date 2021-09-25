package nextstep.jdbc;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JdbcTemplateTest {
    private static final User SALLY = new User(1L, "sally", "password", "sally@hi");

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        dataSource.setUser("");
        dataSource.setPassword("");

        String sql = "create table if not exists users (\n" +
                "    id bigint auto_increment,\n" +
                "    account varchar(100) not null,\n" +
                "    password varchar(100) not null,\n" +
                "    email varchar(100) not null,\n" +
                "    primary key(id)\n" +
                ");\n";
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void insert() {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        assertDoesNotThrow(() -> jdbcTemplate.insert(sql, "sally", "password", "sally@hi"));
    }

    @Test
    void update() {
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.insert(insertSql, "sally", "password", "sally@hi");

        String updateSql = "update users set account = ?, password = ?, email = ? where id = ?";
        assertDoesNotThrow(() -> jdbcTemplate.update(updateSql, "sally", "password", "sally@hi", "1"));
    }

    @Test
    void query() {
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.insert(insertSql, "sally", "password", "sally@hi");

        String querySql = "select id, account, password, email from users";
        List<User> userList = jdbcTemplate.query(querySql, makeUserRowMapper());
        assertThat(userList).usingRecursiveComparison()
                .isEqualTo(List.of(SALLY));
    }

    @Test
    void queryObject() {
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.insert(insertSql, "sally", "password", "sally@hi");

        String querySql = "select id, account, password, email from users where id = ?";
        User user = jdbcTemplate.queryObject(querySql, makeUserRowMapper(), 1L);
        assertThat(user).usingRecursiveComparison()
                .isEqualTo(SALLY);
    }

    private RowMapper<User> makeUserRowMapper() {
        return rs -> {
            long id = rs.getLong("id");
            String account = rs.getString("account");
            String password = rs.getString("password");
            String email = rs.getString("email");
            return new User(id, account, password, email);
        };
    }

    private static class User {
        private final Long id;
        private final String account;
        private final String password;
        private final String email;

        public User(Long id, String account, String password, String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }
    }
}
