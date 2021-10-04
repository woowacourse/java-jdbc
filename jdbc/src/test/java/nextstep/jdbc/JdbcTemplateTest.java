package nextstep.jdbc;

import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("update 메서드가 끝나고 자원이 잘 반환된다.")
    void verifyResourceCloseWhenUpdate() throws SQLException {
        // given
        String sql = "insert into user (name) values (oz)";
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        // when
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        jdbcTemplate.update(sql);

        // then
        verify(connection).close();
        verify(preparedStatement).close();
    }


    @Test
    @DisplayName("query 메서드가 끝나고 자원이 잘 반환된다.")
    void verifyResourceCloseWhenQuery() throws SQLException {
        // given
        String sql = "select * from user";
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        // when
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate.query(sql, null);

        // then
        verify(connection).close();
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @Test
    @DisplayName("update 결과값으로 1이 나온다.")
    void update() throws SQLException {
        // given
        String sql = "insert into user (name) values (oz)";
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        // when
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        final int actual = jdbcTemplate.update(sql);

        // then
        assertThat(actual).isEqualTo(1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("query 결과값 가져오는 것을 성공한다.")
    void query() throws SQLException {
        // given
        String sql = "select * from user";
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        RowMapper<TestUser> rowMapper = mock(RowMapper.class);

        // when
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(rowMapper.mapRow(any(), anyInt())).thenReturn(new TestUser());
        final List<TestUser> testUsers = jdbcTemplate.query(sql, rowMapper);

        // then
        assertThat(testUsers).hasSize(2);
        verify(rowMapper, times(2)).mapRow(any(), anyInt());
    }

    @Test
    @DisplayName("queryForObject 결과값이 1개 일 때 성공한다.")
    void queryForObjectWhenOneResult() throws SQLException {
        // given
        String sql = "select * from user";
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        RowMapper<TestUser> rowMapper = mock(RowMapper.class);

        // when
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(rowMapper.mapRow(any(), anyInt())).thenReturn(new TestUser());
        final TestUser actual = jdbcTemplate.queryForObject(sql, rowMapper);

        // then
        assertThat(actual).isInstanceOf(TestUser.class);
        verify(rowMapper).mapRow(any(), anyInt());
    }


    @Test
    @DisplayName("queryForObject 결과값이 2개 이상일 때 실패한다.")
    void queryForObjectWhenResultsOverTwo() throws SQLException {
        // given
        String sql = "select * from user";
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        RowMapper<TestUser> rowMapper = (rs, rowNum) -> new TestUser();

        // when
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        // then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper))
                .isInstanceOf(TooManyResultsException.class);
    }

    @Test
    @DisplayName("queryForObject 결과값이 없을 때 실패한다.")
    void queryForObjectWhenResultIsEmpty() throws SQLException {
        // given
        String sql = "select * from user";
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        RowMapper<TestUser> rowMapper = (rs, rowNum) -> new TestUser();

        // when
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper))
                .isInstanceOf(EmptyResultException.class);
    }

    private static class TestUser {
    }
}