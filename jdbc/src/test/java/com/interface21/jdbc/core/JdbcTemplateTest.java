package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.helper.TestUser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    static final String INSERT_SQL = "insert into test_users (account) values (?)";
    static final String SELECT_ALL_SQL = "select id, account from test_users";
    static final String SELECT_BY_ID_SQL = "select id, account from test_users where id = ?";
    static final RowMapper<TestUser> TEST_USER_ROW_MAPPER = rs -> new TestUser(
            rs.getLong("id"),
            rs.getString("account"));

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("해제된 connection에 대해 preparedStatement를 불러오려는 경우 예외가 발생한다.")
    @Test
    void should_throwException_when_getPreparedStatementOfClosedConnection() throws SQLException {
        // given
        doThrow(SQLException.class).when(connection).prepareStatement(anyString());

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.update(INSERT_SQL))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("해제된 preparedStatement에 대해 setObject를 수행하는 경우 예외가 발생한다.")
    @Test
    void should_throwException_when_setObjectByClosedPreparedStatement() throws SQLException {
        // given
        doThrow(SQLException.class).when(preparedStatement).setObject(anyInt(), any(Object.class));

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SELECT_BY_ID_SQL, TEST_USER_ROW_MAPPER, 1L))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("executeUpdate를 통해 조회 쿼리를 수행하는 경우 예외가 발생한다.")
    @Test
    void should_throwException_when_executeUpdateWithSelectQuery() throws SQLException {
        // given
        doThrow(SQLException.class).when(preparedStatement).executeUpdate();

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.update(SELECT_ALL_SQL))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("삽입 쿼리를 실행하면 파라미터 주입 후 쿼리가 실행된다.")
    @Test
    void should_setObjectAndExecute_when_executeInsertQuery() throws SQLException {
        // when
        jdbcTemplate.update(INSERT_SQL, "ever");

        // then
        verify(preparedStatement).setObject(1, "ever");
        verify(preparedStatement).executeUpdate();
    }

    @DisplayName("단건 조회 쿼리를 실행하면 해당 데이터를 조회할 수 있다.")
    @Test
    void executeQueryForObject() throws SQLException {
        // given
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("ever");

        // when
        TestUser user = jdbcTemplate.queryForObject(SELECT_BY_ID_SQL, TEST_USER_ROW_MAPPER, 1L);

        // then
        verify(preparedStatement).setObject(1, 1L);
        verify(preparedStatement).executeQuery();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getAccount()).isEqualTo("ever");
    }

    @DisplayName("여러건 조회 쿼리를 실행하면 해당 데이터 목록을 조회할 수 있다.")
    @Test
    void executeQuery() throws SQLException {
        // given
        jdbcTemplate.update(INSERT_SQL, "ever1");
        jdbcTemplate.update(INSERT_SQL, "ever2");

        when(resultSet.next()).thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        // when
        List<TestUser> testUsers = jdbcTemplate.query(SELECT_ALL_SQL, TEST_USER_ROW_MAPPER);

        // then
        verify(preparedStatement).executeQuery();
        assertThat(testUsers).hasSize(2);
    }
}
