package com.interface21.jdbc.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.RowMapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
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

    @Test
    public void testQuery() throws Exception {
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

    @Test
    public void testUpdate() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        int rowsAffected = jdbcTemplate.update("update users set name = ? where id = ?", "클로버지니아", 1);

        assertThat(rowsAffected)
                .isEqualTo(1);
    }

    @Test
    public void testQueryForObject() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("킹로버");

        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("name");

        String result = jdbcTemplate.queryForObject("select name from users where id = ?", rowMapper, 1);

        assertThat(result)
                .isEqualTo("킹로버");
    }

    @Test
    public void testQueryForObjectMultipleResults() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("name");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from users where id = ?", rowMapper, 1))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("조회하려는 데이터가 여러 개입니다.");
    }
}
