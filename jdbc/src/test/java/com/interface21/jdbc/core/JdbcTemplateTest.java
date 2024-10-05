package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<TestUser> ROW_MAPPER = rs ->
            new TestUser(rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email"));

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement preparedStatement;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.connection = mock(Connection.class);
        this.preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    void write_쿼리를_실행한다() throws SQLException {
        // given
        String sql = "INSERT INTO test_users (account, password, email) VALUES (?, ?, ?)";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        // when
        jdbcTemplate.update(sql, "prin", "1q2w3e4r!@", "prin@gmail.com");

        // then
        assertAll(
                () -> verify(preparedStatement).setObject(1, "prin"),
                () -> verify(preparedStatement).setObject(2, "1q2w3e4r!@"),
                () -> verify(preparedStatement).setObject(3, "prin@gmail.com"),
                () -> verify(preparedStatement).executeUpdate()
        );
    }

    @Test
    void read_쿼리로_n개의_데이터를_조회한다() throws SQLException {
        // given
        String sql = "SELECT id, account, password, email FROM test_users";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("account")).thenReturn("prin", "waterfall");
        when(resultSet.getString("password")).thenReturn("1q2w3e4r!@", "1q2w3e4r!@");
        when(resultSet.getString("email")).thenReturn("prin@gmail.com", "waterfall@gmail.com");
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // when
        List<TestUser> testUsers = jdbcTemplate.query(sql, ROW_MAPPER);

        // then
        assertAll(
                () -> verify(preparedStatement).executeQuery(),
                () -> assertThat(testUsers).hasSize(2),
                () -> assertThat(testUsers).extracting("id").containsExactly(1L, 2L),
                () -> assertThat(testUsers).extracting("account").containsExactly("prin", "waterfall"),
                () -> assertThat(testUsers).extracting("password").containsExactly("1q2w3e4r!@", "1q2w3e4r!@"),
                () -> assertThat(testUsers).extracting("email").containsExactly("prin@gmail.com", "waterfall@gmail.com"),
                () -> verify(resultSet).close()
        );
    }

    @Test
    void read_쿼리로_한_개의_데이터를_조회한다() throws SQLException {
        // given
        String sql = "SELECT id, account, password, email FROM test_users WHERE id = ?";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("prin");
        when(resultSet.getString("password")).thenReturn("1q2w3e4r!@");
        when(resultSet.getString("email")).thenReturn("prin@gmail.com");
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // when
        TestUser testUser = jdbcTemplate.queryForObject(sql, ROW_MAPPER, 1L);

        // then
        assertAll(
                () -> verify(preparedStatement).setObject(1, 1L),
                () -> verify(preparedStatement).executeQuery(),
                () -> assertThat(testUser.id()).isEqualTo(1L),
                () -> assertThat(testUser.account()).isEqualTo("prin"),
                () -> assertThat(testUser.password()).isEqualTo("1q2w3e4r!@"),
                () -> assertThat(testUser.email()).isEqualTo("prin@gmail.com"),
                () -> verify(resultSet).close()
        );
    }

    @Test
    void read_쿼리로_한_개의_데이터_조회_시_데이터가_없으면_예외가_발생한다() throws SQLException {
        // given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject("query", ROW_MAPPER, 1L))
                .isExactlyInstanceOf(EmptyResultDataAccessException.class)
                .hasMessage("Incorrect result size. expected size: 1, actual size: 0");
    }

    @Test
    void read_쿼리로_한_개의_데이터_조회_시_데이터가_2개_이상이면_예외가_발생한다() throws SQLException {
        // given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject("query", ROW_MAPPER, 1L))
                .isExactlyInstanceOf(IncorrectResultSizeDataAccessException.class)
                .hasMessage("Incorrect result size. expected size: 1, actual size: 2");
    }

    private record TestUser(Long id, String account, String password, String email) {
    }
}
