package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class JdbcTemplateTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement pstmt;
    @Mock
    private ResultSet resultSet;
    @Mock
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

    @DisplayName("query() : 1개 이상의 결과를 조회한다.")
    @Test
    void query() throws SQLException {
        String sql = "SELECT * FROM test_user";
        TestUser kaki = new TestUser(1L, "kaki");
        TestUser aru = new TestUser(2L, "aru");
        when(resultSet.next()).thenReturn(true, true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn(kaki, aru);

        List<TestUser> result = jdbcTemplate.query(sql, rowMapper);

        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).containsExactly(kaki, aru),
                () -> verify(connection).prepareStatement(sql),
                () -> verify(pstmt).executeQuery(),
                () -> verify(resultSet, times(3)).next(),
                () -> verify(rowMapper, times(2)).mapRow(resultSet),
                () -> verify(connection).close(),
                () -> verify(pstmt).close(),
                () -> verify(resultSet).close()
        );
    }

    @DisplayName("queryForObject() : 1개의 결과 조회에 성공한다.")
    @Test
    void successByQueryForObject() throws SQLException {
        String sql = "SELECT * FROM test_user";
        TestUser kaki = new TestUser(1L, "kaki");
        when(resultSet.next()).thenReturn(true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn(kaki);

        TestUser testUser = jdbcTemplate.queryForObject(sql, rowMapper);

        assertAll(
                () -> assertThat(testUser).isNotNull(),
                () -> assertThat(testUser).usingRecursiveComparison().isEqualTo(kaki),
                () -> verify(connection).prepareStatement(sql),
                () -> verify(pstmt).executeQuery(),
                () -> verify(resultSet, times(2)).next(),
                () -> verify(rowMapper).mapRow(resultSet),
                () -> verify(connection).close(),
                () -> verify(pstmt).close(),
                () -> verify(resultSet).close()
        );
    }

    @DisplayName("queryForObject() : 1개 이상의 결과가 조회되면 예외가 발생한다.")
    @Test
    void failByQueryForObject() throws SQLException {
        String sql = "SELECT * FROM test_user";
        TestUser kaki = new TestUser(1L, "kaki");
        TestUser aru = new TestUser(2L, "aru");
        when(resultSet.next()).thenReturn(true, true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn(kaki, aru);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                .hasMessage("1개의 결과를 예상했지만 2개의 결과가 조회되었습니다.");
    }

    @DisplayName("queryForObject() : 조회된 결과가 없으면 예외가 발생한다.")
    @Test
    void failByQueryForObjectNull() throws SQLException {
        String sql = "SELECT * FROM test_user";
        when(resultSet.next()).thenReturn(false);
        when(rowMapper.mapRow(resultSet)).thenReturn(null);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                .hasMessage("1개의 결과를 예상했지만 0개의 결과가 조회되었습니다.");
    }

    @DisplayName("update() : 저장된 User의 값을 변경한다.")
    @Test
    void update() throws SQLException {
        String sql = "UPDATE test_user SET name = ? WHERE id = ?";
        when(pstmt.executeUpdate()).thenReturn(1);

        jdbcTemplate.update(sql, "aru", 1);

        assertAll(
                () -> verify(connection).prepareStatement(sql),
                () -> verify(pstmt).setObject(1, "aru"),
                () -> verify(pstmt).setObject(2, 1),
                () -> verify(pstmt).executeUpdate(),
                () -> verify(connection).close(),
                () -> verify(pstmt).close(),
                () -> verify(resultSet).close()
        );
    }
}
