package nextstep.jdbc;

import nextstep.jdbc.core.JdbcTemplate;
import nextstep.jdbc.core.RowMapper;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {
    private static DataSource mockDataSource = mock(DataSource.class);
    private static Connection mockConnection = mock(Connection.class);
    private static PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
    private static JdbcTemplate jdbcTemplate = new JdbcTemplate(mockDataSource);

    public static final RowMapper<TestUser> TEST_USER_ROW_MAPPER = (rs, rowNum) -> new TestUser(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    @BeforeAll
    static void setMockArgumentInJdbcTemplate() throws SQLException {
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    }

    @Test
    @DisplayName("복수의 로우를 얻고 매핑한다.")
    void query() throws SQLException {
        // given
        Object[][] mockData = {{1L, "account1", "name1", "email1"}, {2L, "account2", "name2", "email2"}};
        ResultSet mockTestUserResultSet = new TestUserMockResultSetBuilder(mockData).build();

        when(mockPreparedStatement.executeQuery()).thenReturn(mockTestUserResultSet);

        // when
        List<TestUser> testUsers = jdbcTemplate.query("SELECT * FROM users", TEST_USER_ROW_MAPPER);

        // then
        assertThat(testUsers).hasSize(2);
    }

    @Test
    @DisplayName("단수의 로우를 얻는다.")
    public void queryForObject() throws SQLException {
        // given
        TestUser expected = new TestUser(1L, "account", "name", "email");

        Object[][] mockData = {{1L, "account", "name", "email"}};
        ResultSet mockTestUserResultSet = new TestUserMockResultSetBuilder(mockData).build();

        when(mockPreparedStatement.executeQuery()).thenReturn(mockTestUserResultSet);

        // when
        TestUser actual = jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", TEST_USER_ROW_MAPPER, 1L);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("queryForObject()는 복수의 결과값이 나오면 예외를 발생시킨다.")
    public void queryForObjectWhere() throws SQLException {
        // given
        Object[][] mockData = {{1L, "account1", "name", "email1"}, {2L, "account2", "name", "email2"}};
        ResultSet mockTestUserResultSet = new TestUserMockResultSetBuilder(mockData).build();

        when(mockPreparedStatement.executeQuery()).thenReturn(mockTestUserResultSet);

        // then
        assertThatThrownBy(() ->
                jdbcTemplate.queryForObject("SELECT * FROM users WHERE name = ?", TEST_USER_ROW_MAPPER, "name"))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    @DisplayName("update 쿼리는 반영된 Row의 개수만큼을 int 값으로 리턴한다.")
    void update() throws SQLException {
        // given
        int assumedAffectedNumber = 11;
        long assumedId = 1L;
        when(mockPreparedStatement.executeUpdate()).thenReturn(assumedAffectedNumber);

        // then
        assertThat(jdbcTemplate.update("UPDATE users SET account=?, password=?, email=? WHERE id=?", assumedId)).isEqualTo(assumedAffectedNumber);
    }
}
