package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private Connection connection;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

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

    @DisplayName("전달받은 sql과 파라미터의 조건에 맞는 List를 반환한다.")
    @Test
    void query() throws SQLException {
        //given
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(resultSet.getLong(1)).thenReturn(1L).thenReturn(2L);
        when(resultSet.getString(2)).thenReturn("pola").thenReturn("pola");
        String sql = "select id, name from test_users where name = ?";
        String targetName = "pola";

        // when
        List<TestUser> testUsers = jdbcTemplate.query(sql, getRowMapper(), targetName);

        // then
        verify(preparedStatement).setString(1, targetName);
        assertAll(
                () -> assertThat(testUsers.size()).isEqualTo(2),
                () -> assertThat(testUsers).containsExactlyInAnyOrder(new TestUser(1L, "pola"), new TestUser(2L, "pola"))
        );
    }

    private RowMapper<TestUser> getRowMapper() {
        return rs -> new TestUser(
                rs.getLong(1),
                rs.getString(2)
        );
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

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            TestUser testUser = (TestUser) o;
            return Objects.equals(id, testUser.id) && Objects.equals(name, testUser.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
    }
}
