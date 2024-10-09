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
        ResultSet rs = mock(ResultSet.class);
        Person expected = new Person(1, "myungoh", 25);
        when(rs.getObject("id")).thenReturn(expected.id());
        when(rs.getObject("name")).thenReturn(expected.name());
        when(rs.getObject("age")).thenReturn(expected.age());
        RowMapper<Person> personRowMapper = new ReflectionRowMapper<>(Person.class);

        // when
        Person person = personRowMapper.mapRow(rs);

        // then
        assertThat(person).isEqualTo(expected);
    }

    @DisplayName("클래스의 Camel Case 필드를 Snake Case로 변환하여 매핑한다.")
    @Test
    void mapRow_camelToSnake() throws SQLException {
        // given
        ResultSet rs = mock(ResultSet.class);
        Snake expected = new Snake(1, "myungoh");
        when(rs.getObject("id")).thenReturn(expected.id());
        when(rs.getObject("owner_name")).thenReturn(expected.ownerName());
        RowMapper<Snake> snakeRowMapper = new ReflectionRowMapper<>(Snake.class);

        // when
        Snake snake = snakeRowMapper.mapRow(rs);

        // then
        assertThat(snake).isEqualTo(expected);
    }
}
