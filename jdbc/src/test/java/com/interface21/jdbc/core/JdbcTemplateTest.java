package com.interface21.jdbc.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.jdbc.utils.DataSourceConfig;
import com.interface21.jdbc.utils.DatabasePopulatorUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource = DataSourceConfig.getInstance();
    private JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource, new PreparedStatementSetter());

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.createTables(dataSource);
    }

    @AfterEach
    void cleanUp() {
        DatabasePopulatorUtils.truncateTables(dataSource);
    }

    @DisplayName("insert 쿼리 실행 시 값이 저장된다.")
    @Test
    void insert() {
        // given
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        Object[] arguments = new String[]{"ddang", "password", "ddang@email.com"};

        // when
        jdbcTemplate.update(insertSql, arguments);

        String selectSql = "select id, account, password, email from users";
        List<User> users = jdbcTemplate.query(selectSql, this::mapRow);

        // then
        assertAll(
                () -> assertThat(users.size()).isEqualTo(1),
                () -> assertThat(users.get(0).id).isEqualTo(1L),
                () -> assertThat(users.get(0).account).isEqualTo("ddang"),
                () -> assertThat(users.get(0).password).isEqualTo("password"),
                () -> assertThat(users.get(0).email).isEqualTo("ddang@email.com")
        );
    }

    @DisplayName("queryForObject 메서드를 통해 조건에 맞는 row를 단건 조회할 수 있다.")
    @Test
    void queryForObject() {
        // given
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        Object[] arguments = new String[]{"ddang", "password", "ddang@email.com"};

        jdbcTemplate.update(sql, arguments);

        // when
        String sql2 = "select id, account, password, email from users where id = ?";
        User user = jdbcTemplate.queryForObject(sql2, this::mapRow, 1L);

        // then
        assertAll(
                () -> assertThat(user.id).isEqualTo(1L),
                () -> assertThat(user.account).isEqualTo("ddang"),
                () -> assertThat(user.password).isEqualTo("password"),
                () -> assertThat(user.email).isEqualTo("ddang@email.com")
        );
    }

    @DisplayName("queryForObject 단건 조회 시 두 개 이상의 row가 조회되는 경우, 예외가 발생한다.")
    @Test
    void queryForObjectMultipleResults() {
        // given
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        Object[] arguments = new String[]{"ddang", "password", "ddang@email.com"};

        jdbcTemplate.update(sql, arguments);
        jdbcTemplate.update(sql, arguments);

        // when & then
        String sql2 = "select id, account, password, email from users where account = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql2, this::mapRow, "ddang"))
                .isInstanceOf(JdbcException.class)
                .hasMessage("multiple rows found.");
    }

    @DisplayName("query 메서드를 통해 조건에 맞는 row를 모두 조회할 수 있다.")
    @Test
    void queryWithCondition() {
        // given
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        Object[] arguments = new String[]{"ddang", "password", "ddang@email.com"};

        jdbcTemplate.update(sql, arguments);
        jdbcTemplate.update(sql, arguments);
        jdbcTemplate.update(sql, arguments);

        // when
        String sql2 = "select id, account, password, email from users where id >= ?";
        List<User> users = jdbcTemplate.query(sql2, this::mapRow, 2);

        // then
        assertThat(users.size()).isEqualTo(2);
    }

    @DisplayName("query 메서드에 조건을 지정하지 않는 경우, 모든 row를 조회할 수 있다.")
    @Test
    void queryForAllRows() {
        // given
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        Object[] arguments = new String[]{"ddang", "password", "ddang@email.com"};

        jdbcTemplate.update(sql, arguments);
        jdbcTemplate.update(sql, arguments);
        jdbcTemplate.update(sql, arguments);

        // when
        String sql2 = "select id, account, password, email from users";
        List<User> users = jdbcTemplate.query(sql2, this::mapRow);

        // then
        assertThat(users.size()).isEqualTo(3);
    }

    @DisplayName("update 쿼리 실행 시 변경된 값이 조회된다.")
    @Test
    void update() {
        // given
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        Object[] arguments = new String[]{"ddang", "password", "ddang@email.com"};

        jdbcTemplate.update(insertSql, arguments);

        // when
        String updateSql = "update users set account=?, password=?, email=? where id=?";
        jdbcTemplate.update(updateSql, "ddang", "newPassword", "ddang@email.com", 1L);

        String selectSql = "select id, account, password, email from users where id=?";
        User user = jdbcTemplate.queryForObject(selectSql, this::mapRow, 1L);

        // then
        assertThat(user.password).isEqualTo("newPassword");
    }

    class User {
        private Long id;
        private final String account;
        private String password;
        private final String email;

        public User(Long id, String account, String password, String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }
    }

    private User mapRow(final ResultSet resultSet) {
        try {
            return new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email"));
        } catch (SQLException e) {
            throw new JdbcException("An error occurred during mapping row into object.", e);
        }
    }
}
