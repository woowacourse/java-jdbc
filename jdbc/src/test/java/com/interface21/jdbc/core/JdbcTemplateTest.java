package com.interface21.jdbc.core;

import com.interface21.jdbc.core.sample.Person;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JdbcTemplateTest {

    private static final RowMapper<Person> ROW_MAPPER = rs -> new Person(
            rs.getLong(1),
            rs.getString(2),
            rs.getInt(3)
    );

    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    private Connection connection;
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        preparedStatement = Mockito.mock(PreparedStatement.class);
        connection = Mockito.mock(Connection.class);
        dataSource = Mockito.mock(DataSource.class);
        resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(false);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("입력된 쿼리에 따라 업데이트를 수행한다.")
    @Test
    void update() throws SQLException {
        // given
        String sql = "insert into people (name, age) values (?, ?)";

        // when
        jdbcTemplate.update(sql, "name", 15);

        // then
        Mockito.verify(dataSource).getConnection();
        Mockito.verify(connection).prepareStatement(sql);
        Mockito.verify(preparedStatement).setObject(1, "name");
        Mockito.verify(preparedStatement).setObject(2, 15);
        Mockito.verify(preparedStatement).executeUpdate();
    }

    @DisplayName("입력된 RowMapper에 따라 값을 가져온다.")
    @Test
    void query() throws SQLException {
        // given
        String sql = "select id, name, age from people where id = ?";

        // when
        jdbcTemplate.query(sql, ROW_MAPPER);

        // then
        Mockito.verify(dataSource).getConnection();
        Mockito.verify(connection).prepareStatement(sql);
        Mockito.verify(preparedStatement).executeQuery();
        Mockito.verify(resultSet).next();
    }

    @DisplayName("입력된 RowMapper에 따라 리스트를 가져온다.")
    @Test
    void queryForList() throws SQLException {
        // given
        String sql = "select id, name, age from people where id = ?";

        // when
        jdbcTemplate.queryForList(sql, ROW_MAPPER);

        // then
        Mockito.verify(dataSource).getConnection();
        Mockito.verify(connection).prepareStatement(sql);
        Mockito.verify(preparedStatement).executeQuery();
        Mockito.verify(resultSet).next();
    }
}
