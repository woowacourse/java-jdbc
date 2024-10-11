package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.core.sample.Person;
import com.interface21.jdbc.core.sample.Snake;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReflectionRowMapperTest {

    @DisplayName("리플렉션으로 반환값을 매핑한다.")
    @Test
    void mapRow() throws SQLException {
        // given
        ResultSet resultSet = mock(ResultSet.class);
        Person expected = new Person(1L, "myungoh", 25);
        when(resultSet.getObject("id")).thenReturn(expected.getId());
        when(resultSet.getObject("name")).thenReturn(expected.getName());
        when(resultSet.getObject("age")).thenReturn(expected.getAge());
        RowMapper<Person> personRowMapper = new ReflectionRowMapper<>(Person.class);

        // when
        Person person = personRowMapper.mapRow(resultSet);

        // then
        assertThat(person).isEqualTo(expected);
    }

    @DisplayName("클래스의 Camel Case 필드를 Snake Case로 변환하여 매핑한다.")
    @Test
    void mapRow_camelToSnake() throws SQLException {
        // given
        ResultSet resultSet = mock(ResultSet.class);
        Snake expected = new Snake(1L, "myungoh");
        when(resultSet.getObject("id")).thenReturn(expected.getId());
        when(resultSet.getObject("owner_name")).thenReturn(expected.getOwnerName());
        RowMapper<Snake> snakeRowMapper = new ReflectionRowMapper<>(Snake.class);

        // when
        Snake snake = snakeRowMapper.mapRow(resultSet);

        // then
        assertThat(snake).isEqualTo(expected);
    }
}
