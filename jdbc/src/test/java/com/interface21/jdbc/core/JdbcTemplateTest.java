package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private Map<Long, TestEntity> database;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        database = new HashMap<>();

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
    }

    @Test
    @DisplayName("jdbcTemplate를 통해 업데이트 할 수 있다.")
    void executeUpdate() throws SQLException {
        // given
        database.put(1L, new TestEntity("name"));
        database.put(2L, new TestEntity("otherName"));
        when(preparedStatement.executeUpdate()).thenReturn(updateDatabase("name", "newName"));
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        int updated = jdbcTemplate.executeUpdate("UPDATE TestEntity SET name = ? WHERE name = ?", "newName", "name");

        // then
        assertThat(updated).isEqualTo(1);
    }

    @Test
    @DisplayName("jdbcTemplate를 통해 엔티티를 찾을 수 있다.")
    void execute() throws SQLException {
        // given
        database.put(1L, new TestEntity("name"));
        database.put(2L, new TestEntity("otherName"));
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(any())).thenReturn("name");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        TestEntity executed = jdbcTemplate.execute("SELECT te FROM TestEntity te WHERE te.name = ?",
                rs -> new TestEntity(rs.getString("name")),
                "name");

        // then
        assertThat(executed.getName()).isEqualTo("name");
    }

    @Test
    @DisplayName("jdbcTemplate를 통해 여러 엔티티를 찾을 수 있다.")
    void executeList() throws SQLException {
        // given
        database.put(1L, new TestEntity("name"));
        database.put(2L, new TestEntity("otherName"));
        database.put(3L, new TestEntity("otherName"));
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(resultSet.getString(any())).thenReturn("otherName");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        List<TestEntity> executed = jdbcTemplate.executeList("SELECT te FROM TestEntity te WHERE te.name = ?",
                rs -> new TestEntity(rs.getString("name")),
                "otherName");

        // then
        assertThat(executed.size()).isEqualTo(2);
        assertThat(executed.get(0).getName()).isEqualTo("otherName");
        assertThat(executed.get(1).getName()).isEqualTo("otherName");
    }

    int updateDatabase(String name, String newName) {
        List<TestEntity> testEntities = database.values().stream()
                .filter(testEntity -> testEntity.getName().equals(name))
                .toList();

        testEntities.forEach(testEntity -> testEntity.setName(newName));
        return testEntities.size();
    }
}
