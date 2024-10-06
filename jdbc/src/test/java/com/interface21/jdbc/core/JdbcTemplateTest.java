package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import samples.TestUser;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet resultSet;
    private RowMapper<TestUser> rowMapper;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        rowMapper = mock(RowMapper.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @DisplayName("데이터로 수정한다.")
    @Test
    void update() throws SQLException {
        // given
        String sql = "UPDATE test_users SET name = ? WHERE id = ?";
        when(pstmt.executeUpdate()).thenReturn(1);

        // when
        jdbcTemplate.update(sql, "lini", 1);

        // then
        assertAll(
                () -> verify(connection).prepareStatement(sql),
                () -> verify(pstmt).setObject(1, "lini"),
                () -> verify(pstmt).setObject(2, 1),
                () -> verify(pstmt).executeUpdate()
        );
    }

    @DisplayName("요구하는 모든 데이터를 조회해온다.")
    @Test
    void query() throws SQLException {
        // given
        String sql = "SELECT * FROM test_users";
        TestUser user = new TestUser(1L, "lini");
        TestUser user2 = new TestUser(2L, "rini");
        when(resultSet.next()).thenReturn(true, true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn(user, user2);

        // when
        List<TestUser> result = jdbcTemplate.query(sql, rowMapper);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> verify(connection).prepareStatement(sql),
                () -> verify(pstmt).executeQuery(),
                () -> verify(resultSet, times(3)).next()
        );
    }

    @DisplayName("1개의 결과만 존재할 때 조회에 성공한다.")
    @Test
    void queryForObject() throws SQLException {
        // given
        String sql = "SELECT * FROM test_users";
        TestUser user = new TestUser(1L, "lini");
        when(resultSet.next()).thenReturn(true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn(user);

        // when
        TestUser testUser = jdbcTemplate.queryForObject(sql, rowMapper);

        // then
        assertAll(
                () -> assertThat(testUser).isNotNull(),
                () -> verify(connection).prepareStatement(sql),
                () -> verify(pstmt).executeQuery(),
                () -> verify(resultSet, times(2)).next()
        );
    }

    @DisplayName("1개 이상의 결과가 존재하면 예외가 발생한다.")
    @Test
    void cannotQueryForObject() throws SQLException {
        // given
        String sql = "SELECT * FROM test_users";
        TestUser user = new TestUser(1L, "lini");
        TestUser user2 = new TestUser(2L, "rini");
        when(resultSet.next()).thenReturn(true, true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn(user, user2);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                .hasMessage("Incorrect result size: expected 1, actual 2");
    }
}
