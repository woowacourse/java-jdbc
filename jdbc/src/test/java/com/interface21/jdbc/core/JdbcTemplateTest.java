package com.interface21.jdbc.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.RowMapper;
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
import org.mockito.MockitoAnnotations;

class JdbcTemplateTest {
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("여러 데이터를 조회하는 쿼리를 처리한다.")
    @Test
    public void query() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString("name")).thenReturn("이은정", "클로버", "지니아");

        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("name");

        List<String> result = jdbcTemplate.query("select name from users", rowMapper);

        assertAll(
                () -> assertThat(result.size())
                        .isEqualTo(3),
                () -> assertThat(result)
                        .isEqualTo(List.of("이은정", "클로버", "지니아"))
        );
    }

    @DisplayName("데이터를 업데이트하는 쿼리를 처리한다.")
    @Test
    public void update() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        int rowsAffected = jdbcTemplate.update("update users set name = ? where id = ?", "클로버지니아", 1);

        assertThat(rowsAffected)
                .isEqualTo(1);
    }

    @DisplayName("한 개의 데이터를 조회하는 쿼리를 처리한다.")
    @Test
    public void queryForObject() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("킹로버");

        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("name");

        String result = jdbcTemplate.queryForObject("select name from users where id = ?", rowMapper, 1);

        assertThat(result)
                .isEqualTo("킹로버");
    }

    @DisplayName("조회하려는 데이터가 여러 개일 경우 예외로 처리한다.")
    @Test
    public void queryForObjectFailedWithMultipleResults() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("name");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from users where id = ?", rowMapper, 1))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("조회하려는 데이터가 여러 개입니다.");
    }

    @DisplayName("사용한 자원은 모두 close한다.")
    @Test
    public void closeAllResources() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString("name")).thenReturn("이은정", "클로버", "지니아");

        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("name");

        jdbcTemplate.query("select name from users", rowMapper);

        assertAll(
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close(),
                () -> verify(resultSet).close()
        );
    }
}
