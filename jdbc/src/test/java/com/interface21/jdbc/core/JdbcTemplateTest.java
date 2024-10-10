package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("업데이트문이 실행된다.")
    void executeQuery() {
        String sql = "INSERT INTO crew (name, reviewer, reviewee) VALUES (?, ?, ?)";
        List<Object> paramList = List.of("atto", "jeje", "daon");

        jdbcTemplate.executeQuery(sql, paramList);

        assertAll(
                () -> verify(preparedStatement, times(1)).setObject(1, "atto"),
                () -> verify(preparedStatement, times(1)).setObject(2, "jeje"),
                () -> verify(preparedStatement, times(1)).setObject(3, "daon"),
                () -> verify(preparedStatement, times(1)).executeUpdate()

        );
    }

    @Test
    @DisplayName("하나의 객체를 만든다.")
    void executeQueryForObject() throws SQLException {
        String sql = "select name, reviewer, reviewee from crew where id = ?";
        List<Object> paramList = List.of(1L);
        ObjectMaker<TestObject> maker = new TestMaker();

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn("atto");
        when(resultSet.getString(2)).thenReturn("jeje");
        when(resultSet.getString(3)).thenReturn("daon");

        TestObject actual = jdbcTemplate.executeQueryForObject(sql, paramList, maker).orElseThrow();
        TestObject expected = new TestObject("atto", "jeje", "daon");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("하나의 객체를 만들 때 결과물이 두개 이상이면 예외가 발생한다.")
    void executeQueryForObjectWithDuplicate() throws SQLException {
        String sql = "select name, reviewer, reviewee from crew where id = ?";
        List<Object> paramList = List.of(1L);
        ObjectMaker<TestObject> maker = new TestMaker();

        when(resultSet.next()).thenReturn(true, true);
        when(resultSet.getString(1)).thenReturn("atto", "jeje");
        when(resultSet.getString(2)).thenReturn("jeje", "rush");
        when(resultSet.getString(3)).thenReturn("daon", "atto");

        assertThatThrownBy(() -> jdbcTemplate.executeQueryForObject(sql, paramList, maker))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결과가 두개 이상 존재합니다.");
    }

    @Test
    @DisplayName("객체의 리스트를 만든다.")
    void executeQueryForObjects() throws SQLException {
        String sql = "select name, reviewer, reviewee from crew";
        List<Object> paramList = List.of(1L);
        ObjectMaker<TestObject> maker = new TestMaker();

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn("atto");
        when(resultSet.getString(2)).thenReturn("jeje");
        when(resultSet.getString(3)).thenReturn("daon");

        List<TestObject> actual = jdbcTemplate.executeQueryForObjects(sql, paramList, maker);
        List<TestObject> expected = List.of(new TestObject("atto", "jeje", "daon"));

        assertThat(actual).isEqualTo(expected);
    }

    private record TestObject(String name, String reviewer, String reviewee) {
    }

    private class TestMaker implements ObjectMaker<TestObject> {

        @Override
        public TestObject make(ResultSet resultSet) throws SQLException {
            return new TestObject(
                    resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getString(3));
        }
    }
}
