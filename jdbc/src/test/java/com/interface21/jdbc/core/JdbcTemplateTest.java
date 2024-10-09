package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.ResultNotSingleException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private PreparedStatement pstmt;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void 쓰기_명령_쿼리를_실행() throws SQLException {
        // given
        int id = 1;
        String name = "ted";
        String sql = "INSERT INTO users (id, name) VALUES (?, ?)";

        // when
        jdbcTemplate.write(sql, id, name);

        // then
        assertAll(
                () -> verify(pstmt).setObject(1, id),
                () -> verify(pstmt).setObject(2, name),
                () -> verify(pstmt).executeUpdate()
        );
    }

    @Test
    void 읽기_명령_쿼리를_실행() throws SQLException {
        // given
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("name")).thenReturn("ted", "lini");

        RowMapper<TestUser> rowMapper = rs -> new TestUser(rs.getInt("id"), rs.getString("name"));
        String sql = "SELECT * FROM users";

        // when
        List<TestUser> expected = jdbcTemplate.readAll(sql, rowMapper);

        // then
        assertAll(
                () -> verify(pstmt).executeQuery(),
                () -> assertThat(expected).hasSize(2),
                () -> assertThat(expected).extracting(TestUser::getId).containsExactlyInAnyOrder(1, 2)
        );
    }

    @Test
    void 단일_읽기_명령_쿼리를_실행() throws SQLException {
        // given
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("ted");

        RowMapper<TestUser> rowMapper = rs -> new TestUser(rs.getInt("id"), rs.getString("name"));
        String sql = "SELECT * FROM users WHERE id = ?";

        // when
        TestUser testUser = jdbcTemplate.read(sql, rowMapper, 1);

        // then
        assertAll(
                () -> verify(pstmt).executeQuery(),
                () -> assertThat(testUser.getId()).isEqualTo(1),
                () -> assertThat(testUser.getName()).isEqualTo("ted")
        );
    }

    @Test
    void 단일_읽기_명령_쿼리_실행시_결과가_여러개일_경우_예외() throws SQLException {
        // given
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("name")).thenReturn("ted", "lini");

        RowMapper<TestUser> rowMapper = rs -> new TestUser(rs.getInt("id"), rs.getString("name"));
        String sql = "SELECT * FROM users";

        // when, when
        assertThatThrownBy(() -> jdbcTemplate.read(sql, rowMapper))
                .isInstanceOf(ResultNotSingleException.class);
    }
}
