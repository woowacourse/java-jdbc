package com.interface21.jdbc.core;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("전달받은 sql과 파라미터를 활용해 PreparedStatement의 executeUpdate 메서드를 호출한다.")
    @Test
    void update() throws SQLException {
        //given
        String sql = "insert into test_users (id, name) values (?, ?)";
        Long id = 1L;
        String name = "pola";

        // when
        jdbcTemplate.update(sql, id, name);

        // then
        verify(preparedStatement).setLong(1, id);
        verify(preparedStatement).setString(2, name);
        verify(preparedStatement).executeUpdate();
    }

    static class TestUser {
        private Long id;
        private String name;

        public TestUser(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
