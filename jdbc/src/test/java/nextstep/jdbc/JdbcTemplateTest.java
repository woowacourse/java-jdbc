package nextstep.jdbc;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JdbcTemplateTest {
    private static final Long ID = 1L;
    private static final String ACCOUNT = "bada";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "bada@gmail.com";
    private static final String NEW_EMAIL = "bada@naver.com";
    private static final User BADA = new User(ID, ACCOUNT, PASSWORD, EMAIL);

    private static final RowMapper<User> rowMapper = rs -> new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4));

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        dataSource.setUser("");
        dataSource.setPassword("");

        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("drop table users if exists");
        }

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
    @DisplayName("update 메서드를 이용해 user를 저장한다.")
    void insert() {
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        assertDoesNotThrow(() -> jdbcTemplate.update(insertSql, ACCOUNT, PASSWORD, EMAIL));
    }

    @Test
    @DisplayName("update 메서드를 이용해 user 정보를 수정한다.")
    void update() {
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, ACCOUNT, PASSWORD, EMAIL);

        String updateSql = "update users set account = ?, password = ?, email = ? where id = ?";
        assertDoesNotThrow(() -> jdbcTemplate.update(updateSql, ACCOUNT, PASSWORD, NEW_EMAIL, ID));
    }

    @Test
    @DisplayName("query 메서드를 이용해 모든 유저들의 정보를 조회한다.")
    void query() {
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, ACCOUNT, PASSWORD, EMAIL);

        String selectSql = "select id, account, password, email from users";
        List<User> userList = jdbcTemplate.query(selectSql, rowMapper);
        assertThat(userList).usingRecursiveComparison()
                .isEqualTo(List.of(BADA));
    }

    @Test
    @DisplayName("queryForObject 메서드를 이용해 id로 특정 유저의 정보를 조회한다.")
    void queryForObject() {
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, ACCOUNT, PASSWORD, EMAIL);

        String selectSql = "select id, account, password, email from users where id = ?";
        User user = jdbcTemplate.queryForObject(selectSql, rowMapper, ID);
        assertThat(user).usingRecursiveComparison()
                .isEqualTo(BADA);
    }

    @Test
    @DisplayName("queryForObject의 결과가 1개가 아니라면 예외를 발생한다.")
    void queryForObject_exception() {
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, ACCOUNT, PASSWORD, EMAIL);
        jdbcTemplate.update(insertSql, ACCOUNT, PASSWORD, NEW_EMAIL);

        String selectSql = "select id, account, password, email from users where account = ?";
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(selectSql, rowMapper, ACCOUNT))
                .isInstanceOf(IllegalArgumentException.class);
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
