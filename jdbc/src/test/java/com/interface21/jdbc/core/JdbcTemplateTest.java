package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.dao.DataAccessException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcTemplate = new JdbcTemplate(TestDataSourceConfig.getInstance());
        TestDataSourceConfig.clearTestData();
    }

    @Test
    void update_정상적인_INSERT_쿼리() {
        // given
        final String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        final String account = "testuser";
        final String password = "password123";
        final String email = "test@example.com";

        // when
        jdbcTemplate.update(sql, account, password, email);

        // then
        // 데이터가 실제로 삽입되었는지 확인
        final String selectSql = "SELECT COUNT(*) FROM users WHERE account = ?";
        final Optional<Integer> count = jdbcTemplate.queryForObject(selectSql,
                rs -> rs.getInt(1), account);
        assertThat(count).isPresent();
        assertThat(count.get()).isEqualTo(1);
    }

    @Test
    void update_정상적인_UPDATE_쿼리() {
        // given
        jdbcTemplate.update("INSERT INTO users (account, password, email) VALUES (?, ?, ?)",
                "updateuser", "oldpassword", "update@example.com");

        final String sql = "UPDATE users SET password = ? WHERE account = ?";
        final String newPassword = "newpassword";
        final String account = "updateuser";

        // when
        jdbcTemplate.update(sql, newPassword, account);

        // then

        // 패스워드가 실제로 업데이트되었는지 확인
        final String selectSql = "SELECT password FROM users WHERE account = ?";
        final Optional<String> actualPassword = jdbcTemplate.queryForObject(selectSql,
                rs -> rs.getString("password"), account);
        assertThat(actualPassword).isPresent();
        assertThat(actualPassword.get()).isEqualTo(newPassword);
    }

    @Test
    void update_정상적인_DELETE_쿼리() {
        // given
        jdbcTemplate.update("INSERT INTO users (account, password, email) VALUES (?, ?, ?)",
                "deleteuser", "password", "delete@example.com");

        final String sql = "DELETE FROM users WHERE account = ?";
        final String account = "deleteuser";

        // when
        jdbcTemplate.update(sql, account);

        // then

        // 데이터가 실제로 삭제되었는지 확인
        final String selectSql = "SELECT COUNT(*) FROM users WHERE account = ?";
        final Optional<Integer> count = jdbcTemplate.queryForObject(selectSql,
                rs -> rs.getInt(1), account);
        assertThat(count).isPresent();
        assertThat(count.get()).isEqualTo(0);
    }

    @Test
    void update_null_SQL_예외() {
        // when & then
        assertThatThrownBy(() -> jdbcTemplate.update(null))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void queryForObject_단일_결과_조회() {
        // given
        jdbcTemplate.update("INSERT INTO users (account, password, email) VALUES (?, ?, ?)",
                "queryuser", "password", "query@example.com");

        final String sql = "SELECT * FROM users WHERE account = ?";
        final UserRowMapper rowMapper = new UserRowMapper();

        // when
        final Optional<User> result = jdbcTemplate.queryForObject(sql, rowMapper, "queryuser");

        // then
        assertThat(result).isPresent();
        final User user = result.get();
        assertThat(user.getAccount()).isEqualTo("queryuser");
        assertThat(user.getEmail()).isEqualTo("query@example.com");
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    void queryForObject_결과가_없는_경우() {
        // given
        final String sql = "SELECT * FROM users WHERE account = ?";
        final UserRowMapper rowMapper = new UserRowMapper();

        // when
        final Optional<User> result = jdbcTemplate.queryForObject(sql, rowMapper, "nonexistent");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void queryForObject_여러_결과_예외() {
        // given
        jdbcTemplate.update("INSERT INTO users (account, password, email) VALUES (?, ?, ?)",
                "duplicate", "password1", "dup1@example.com");
        jdbcTemplate.update("INSERT INTO users (account, password, email) VALUES (?, ?, ?)",
                "duplicate", "password2", "dup2@example.com");

        final String sql = "SELECT * FROM users WHERE account = ?";
        final UserRowMapper rowMapper = new UserRowMapper();

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, "duplicate"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("1행만 기대하지만");
    }

    @Test
    void queryForObject_null_파라미터_예외() {
        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject("SELECT * FROM users", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RowMapper는 null일 수 없습니다");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(null, new UserRowMapper()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SQL 쿼리는 null이거나 빈 문자열일 수 없습니다");
    }

    @Test
    void query_여러_결과_조회() {
        // given
        jdbcTemplate.update("INSERT INTO users (account, password, email) VALUES (?, ?, ?)",
                "user1", "password1", "user1@example.com");
        jdbcTemplate.update("INSERT INTO users (account, password, email) VALUES (?, ?, ?)",
                "user2", "password2", "user2@example.com");
        jdbcTemplate.update("INSERT INTO users (account, password, email) VALUES (?, ?, ?)",
                "user3", "password3", "user3@example.com");

        final String sql = "SELECT * FROM users ORDER BY account";
        final UserRowMapper rowMapper = new UserRowMapper();

        // when
        final List<User> result = jdbcTemplate.query(sql, rowMapper);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("account").containsExactly("user1", "user2", "user3");
        assertThat(result).extracting("email")
                .containsExactly("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    void query_조건부_조회() {
        // given
        jdbcTemplate.update("INSERT INTO users (account, password, email) VALUES (?, ?, ?)",
                "kim1", "password1", "kim1@example.com");
        jdbcTemplate.update("INSERT INTO users (account, password, email) VALUES (?, ?, ?)",
                "kim2", "password2", "kim2@example.com");
        jdbcTemplate.update("INSERT INTO users (account, password, email) VALUES (?, ?, ?)",
                "park1", "password3", "park1@example.com");

        final String sql = "SELECT * FROM users WHERE account LIKE ? ORDER BY account";
        final UserRowMapper rowMapper = new UserRowMapper();

        // when
        final List<User> result = jdbcTemplate.query(sql, rowMapper, "kim%");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("account").containsExactly("kim1", "kim2");
    }

    @Test
    void query_빈_결과() {
        // given
        final String sql = "SELECT * FROM users WHERE account = ?";
        final UserRowMapper rowMapper = new UserRowMapper();

        // when
        final List<User> result = jdbcTemplate.query(sql, rowMapper, "nonexistent");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void query_null_SQL_예외() {
        // when & then
        assertThatThrownBy(() -> jdbcTemplate.query(null, new UserRowMapper()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SQL 쿼리는 null이거나 빈 문자열일 수 없습니다");
    }

    @Test
    void 잘못된_SQL_문법_예외() {
        // when & then
        assertThatThrownBy(() -> jdbcTemplate.update("INVALID SQL SYNTAX"))
                .isInstanceOf(DataAccessException.class);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("INVALID SQL SYNTAX", new UserRowMapper()))
                .isInstanceOf(DataAccessException.class);

        assertThatThrownBy(() -> jdbcTemplate.query("INVALID SQL SYNTAX", new UserRowMapper()))
                .isInstanceOf(DataAccessException.class);
    }

    /**
     * User 객체를 매핑하는 RowMapper 구현체
     */
    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet resultSet) throws SQLException {
            return new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email")
            );
        }
    }
}
