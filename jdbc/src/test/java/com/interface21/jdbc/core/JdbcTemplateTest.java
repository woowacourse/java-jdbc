package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.core.sample.Person;
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

    private static final RowMapper<Person> ROW_MAPPER = resultSet -> new Person(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getInt(3)
    );

    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    private Connection connection;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        preparedStatement = mock(PreparedStatement.class);
        connection = mock(Connection.class);
        resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(mock(DataSource.class));
    }

    @DisplayName("입력된 쿼리에 따라 업데이트를 수행한다.")
    @Test
    void update() throws SQLException {
        // given
        String sql = "insert into people (name, age) values (?, ?)";

        // when
        int updated = jdbcTemplate.update(connection, sql, "name", 15);

        // then
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).setObject(1, "name");
        verify(preparedStatement).setObject(2, 15);
        verify(preparedStatement).executeUpdate();
        assertThat(updated).isEqualTo(0);
    }

    @DisplayName("입력된 쿼리와 RowMapper에 따라 값을 가져온다.")
    @Test
    void queryForObject() throws SQLException {
        // given
        String sql = "select id, name, age from people where id = ?";
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong(1)).thenReturn(1L);
        when(resultSet.getString(2)).thenReturn("myungoh");
        when(resultSet.getInt(3)).thenReturn(25);

        // when
        Person person = jdbcTemplate.queryForObject(connection, sql, ROW_MAPPER, 1);

        // then
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeQuery();
        verify(resultSet, times(2)).next();
        assertThat(person).isEqualTo(new Person(1L, "myungoh", 25));
    }

    @DisplayName("쿼리로 가져온 값이 0개인 경우 예외를 던진다.")
    @Test
    void queryForObject_none() throws SQLException {
        // given
        String sql = "select id, name, age from people where id = ?";
        when(resultSet.next()).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(connection, sql, ROW_MAPPER, 3))
                .isInstanceOf(EmptyResultDataAccessException.class);
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeQuery();
        verify(resultSet).next();
    }

    @DisplayName("쿼리로 가져온 값이 1개가 아닌 경우 예외가 발생한다.")
    @Test
    void queryForObject_notUnique() throws SQLException {
        // given
        String sql = "select id, name, age from people where age = ?";
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong(1)).thenReturn(1L, 2L);
        when(resultSet.getString(2)).thenReturn("myungoh", "paper");
        when(resultSet.getInt(3)).thenReturn(25, 25);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(connection, sql, ROW_MAPPER, 25))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeQuery();
        verify(resultSet, times(3)).next();
    }

    @DisplayName("입력된 쿼리와 RowMapper에 따라 리스트를 가져온다.")
    @Test
    void queryForList() throws SQLException {
        // given
        String sql = "select id, name, age from people where age = 25";
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong(1)).thenReturn(1L, 2L);
        when(resultSet.getString(2)).thenReturn("myungoh", "paper");
        when(resultSet.getInt(3)).thenReturn(25, 25);

        // when
        List<Person> people = jdbcTemplate.queryForList(connection, sql, ROW_MAPPER);

        // then
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeQuery();
        verify(resultSet, times(3)).next();
        assertThat(people).contains(new Person(1L, "myungoh", 25), new Person(2L, "paper", 25));
    }
}
