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
    void executeUpdate_정상적인_INSERT_쿼리() {
        // given
        final String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        final String account = "testuser";
        final String password = "password123";
        final String email = "test@example.com";

        // when
        final int result = jdbcTemplate.executeUpdate(sql, account, password, email);

        // then
        assertThat(result).isEqualTo(1);
        
        // 데이터가 실제로 삽입되었는지 확인
        final String selectSql = "SELECT COUNT(*) FROM users WHERE account = ?";
        final Optional<Integer> count = jdbcTemplate.executeQueryForObject(selectSql, 
            rs -> rs.getInt(1), account);
        assertThat(count).isPresent();
        assertThat(count.get()).isEqualTo(1);
    }

    @Test
    void executeUpdate_정상적인_UPDATE_쿼리() {
        // given
        jdbcTemplate.executeUpdate("INSERT INTO users (account, password, email) VALUES (?, ?, ?)", 
            "updateuser", "oldpassword", "update@example.com");
        
        final String sql = "UPDATE users SET password = ? WHERE account = ?";
        final String newPassword = "newpassword";
        final String account = "updateuser";

        // when
        final int result = jdbcTemplate.executeUpdate(sql, newPassword, account);

        // then
        assertThat(result).isEqualTo(1);
        
        // 패스워드가 실제로 업데이트되었는지 확인
        final String selectSql = "SELECT password FROM users WHERE account = ?";
        final Optional<String> actualPassword = jdbcTemplate.executeQueryForObject(selectSql, 
            rs -> rs.getString("password"), account);
        assertThat(actualPassword).isPresent();
        assertThat(actualPassword.get()).isEqualTo(newPassword);
    }

    @Test
    void executeUpdate_정상적인_DELETE_쿼리() {
        // given
        jdbcTemplate.executeUpdate("INSERT INTO users (account, password, email) VALUES (?, ?, ?)", 
            "deleteuser", "password", "delete@example.com");
        
        final String sql = "DELETE FROM users WHERE account = ?";
        final String account = "deleteuser";

        // when
        final int result = jdbcTemplate.executeUpdate(sql, account);

        // then
        assertThat(result).isEqualTo(1);
        
        // 데이터가 실제로 삭제되었는지 확인
        final String selectSql = "SELECT COUNT(*) FROM users WHERE account = ?";
        final Optional<Integer> count = jdbcTemplate.executeQueryForObject(selectSql, 
            rs -> rs.getInt(1), account);
        assertThat(count).isPresent();
        assertThat(count.get()).isEqualTo(0);
    }

    @Test
    void executeUpdate_null_또는_빈_SQL_예외() {
        // when & then
        assertThatThrownBy(() -> jdbcTemplate.executeUpdate(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SQL 쿼리는 null이거나 빈 문자열일 수 없습니다");

        assertThatThrownBy(() -> jdbcTemplate.executeUpdate(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SQL 쿼리는 null이거나 빈 문자열일 수 없습니다");

        assertThatThrownBy(() -> jdbcTemplate.executeUpdate("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SQL 쿼리는 null이거나 빈 문자열일 수 없습니다");
    }

    @Test
    void executeQueryForObject_단일_결과_조회() {
        // given
        jdbcTemplate.executeUpdate("INSERT INTO users (account, password, email) VALUES (?, ?, ?)", 
            "queryuser", "password", "query@example.com");
        
        final String sql = "SELECT * FROM users WHERE account = ?";
        final UserRowMapper rowMapper = new UserRowMapper();

        // when
        final Optional<User> result = jdbcTemplate.executeQueryForObject(sql, rowMapper, "queryuser");

        // then
        assertThat(result).isPresent();
        final User user = result.get();
        assertThat(user.getAccount()).isEqualTo("queryuser");
        assertThat(user.getEmail()).isEqualTo("query@example.com");
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    void executeQueryForObject_결과가_없는_경우() {
        // given
        final String sql = "SELECT * FROM users WHERE account = ?";
        final UserRowMapper rowMapper = new UserRowMapper();

        // when
        final Optional<User> result = jdbcTemplate.executeQueryForObject(sql, rowMapper, "nonexistent");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void executeQueryForObject_여러_결과_예외() {
        // given
        jdbcTemplate.executeUpdate("INSERT INTO users (account, password, email) VALUES (?, ?, ?)", 
            "duplicate", "password1", "dup1@example.com");
        jdbcTemplate.executeUpdate("INSERT INTO users (account, password, email) VALUES (?, ?, ?)", 
            "duplicate", "password2", "dup2@example.com");
        
        final String sql = "SELECT * FROM users WHERE account = ?";
        final UserRowMapper rowMapper = new UserRowMapper();

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.executeQueryForObject(sql, rowMapper, "duplicate"))
            .isInstanceOf(DataAccessException.class)
            .hasMessageContaining("결과가 2개 이상입니다");
    }

    @Test
    void executeQueryForObject_null_파라미터_예외() {
        // when & then
        assertThatThrownBy(() -> jdbcTemplate.executeQueryForObject("SELECT * FROM users", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("RowMapper는 null일 수 없습니다");

        assertThatThrownBy(() -> jdbcTemplate.executeQueryForObject(null, new UserRowMapper()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SQL 쿼리는 null이거나 빈 문자열일 수 없습니다");
    }

    @Test
    void executeQuery_여러_결과_조회() {
        // given
        jdbcTemplate.executeUpdate("INSERT INTO users (account, password, email) VALUES (?, ?, ?)", 
            "user1", "password1", "user1@example.com");
        jdbcTemplate.executeUpdate("INSERT INTO users (account, password, email) VALUES (?, ?, ?)", 
            "user2", "password2", "user2@example.com");
        jdbcTemplate.executeUpdate("INSERT INTO users (account, password, email) VALUES (?, ?, ?)", 
            "user3", "password3", "user3@example.com");
        
        final String sql = "SELECT * FROM users ORDER BY account";
        final UserRowMapper rowMapper = new UserRowMapper();

        // when
        final List<User> result = jdbcTemplate.executeQuery(sql, rowMapper);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("account").containsExactly("user1", "user2", "user3");
        assertThat(result).extracting("email").containsExactly("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    void executeQuery_조건부_조회() {
        // given
        jdbcTemplate.executeUpdate("INSERT INTO users (account, password, email) VALUES (?, ?, ?)", 
            "kim1", "password1", "kim1@example.com");
        jdbcTemplate.executeUpdate("INSERT INTO users (account, password, email) VALUES (?, ?, ?)", 
            "kim2", "password2", "kim2@example.com");
        jdbcTemplate.executeUpdate("INSERT INTO users (account, password, email) VALUES (?, ?, ?)", 
            "park1", "password3", "park1@example.com");
        
        final String sql = "SELECT * FROM users WHERE account LIKE ? ORDER BY account";
        final UserRowMapper rowMapper = new UserRowMapper();

        // when
        final List<User> result = jdbcTemplate.executeQuery(sql, rowMapper, "kim%");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("account").containsExactly("kim1", "kim2");
    }

    @Test
    void executeQuery_빈_결과() {
        // given
        final String sql = "SELECT * FROM users WHERE account = ?";
        final UserRowMapper rowMapper = new UserRowMapper();

        // when
        final List<User> result = jdbcTemplate.executeQuery(sql, rowMapper, "nonexistent");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void executeQuery_null_파라미터_예외() {
        // when & then
        assertThatThrownBy(() -> jdbcTemplate.executeQuery("SELECT * FROM users", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("RowMapper는 null일 수 없습니다");

        assertThatThrownBy(() -> jdbcTemplate.executeQuery(null, new UserRowMapper()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SQL 쿼리는 null이거나 빈 문자열일 수 없습니다");
    }

    @Test
    void 잘못된_SQL_문법_예외() {
        // when & then
        assertThatThrownBy(() -> jdbcTemplate.executeUpdate("INVALID SQL SYNTAX"))
            .isInstanceOf(DataAccessException.class);

        assertThatThrownBy(() -> jdbcTemplate.executeQueryForObject("INVALID SQL SYNTAX", new UserRowMapper()))
            .isInstanceOf(DataAccessException.class);

        assertThatThrownBy(() -> jdbcTemplate.executeQuery("INVALID SQL SYNTAX", new UserRowMapper()))
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
