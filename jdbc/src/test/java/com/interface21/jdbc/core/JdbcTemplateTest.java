package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        preparedStatement = mock(PreparedStatement.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        verify(connection).close();
    }

    @Test
    @DisplayName("업데이트성 쿼리를 올바르게 수행한다.")
    void validUpdate() throws SQLException {
        when(connection.prepareStatement("update test set name = ? where id = ?")).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        jdbcTemplate.update("update test set name = ? where id = ?", "test", 1);

        verify(preparedStatement).setObject(1, "test");
        verify(preparedStatement).setObject(2, 1);
        verify(preparedStatement).close();
    }

    @Test
    @DisplayName("ResultSet을 활용해 여러 결과를 조회한다.")
    void queryMultipleRows() throws SQLException {
        when(connection.prepareStatement("select * from fruit")).thenReturn(preparedStatement);

        // Mocking ResultSet, two rows
        // (id, name) = (1, apple), (2, banana)
        record Fruit(int id, String name) {
        }
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("name")).thenReturn("apple", "banana");
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        RowMapper<Fruit> rowMapper = rs -> new Fruit(rs.getInt("id"), rs.getString("name"));
        assertThat(jdbcTemplate.query("select * from fruit", rowMapper)).containsExactly(
                new Fruit(1, "apple"),
                new Fruit(2, "banana")
        );
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @Test
    @DisplayName("SQLException이 발생하는 경우, 적절한 예외로 변환한다.")
    void convertException() throws SQLException {
        when(connection.prepareStatement("wrong sql grammar")).thenReturn(preparedStatement);
        SQLException sqlException = new SQLException("Bad SQL");
        when(preparedStatement.executeUpdate()).thenThrow(sqlException);

        assertThatThrownBy(() -> jdbcTemplate.update("wrong sql grammar"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(sqlException)
                .hasMessage("Bad SQL");
        verify(preparedStatement).close();
    }

    @Test
    @DisplayName("한 건의 결과를 조회한다.")
    void queryForObjectFetchesOnlyOneRow() throws SQLException {
        when(connection.prepareStatement("select * from fruit where id = ?")).thenReturn(preparedStatement);

        record Fruit(int id, String name) {
        }
        ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("apple");
        RowMapper<Fruit> rowMapper = rs -> new Fruit(rs.getInt("id"), rs.getString("name"));

        assertThat(jdbcTemplate.queryForObject("select * from fruit where id = ?", rowMapper, 1))
                .isEqualTo(new Fruit(1, "apple"));
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @Test
    @DisplayName("단건 조회시 결과가 두 개 이상인 경우 예외를 발생한다.")
    void queryForObjectFetchesMultipleRows() throws SQLException {
        when(connection.prepareStatement("select * from fruit where id = ?")).thenReturn(preparedStatement);

        record Fruit(int id, String name) {
        }
        ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("name")).thenReturn("apple", "banana");
        RowMapper<Fruit> rowMapper = rs -> new Fruit(rs.getInt("id"), rs.getString("name"));

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select * from fruit where id = ?", rowMapper, 1))
                .isInstanceOf(DataSizeMismatchException.class)
                .hasMessage("Expected query result size: 1, fetched 2 rows");
        verify(preparedStatement).close();
        verify(resultSet).close();
    }
}
