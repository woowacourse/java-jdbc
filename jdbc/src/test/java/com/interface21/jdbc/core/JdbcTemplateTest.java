package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import com.interface21.dao.DataAccessException;

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
        verify(preparedStatement).setObject(1, id);
        verify(preparedStatement).setObject(2, name);
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
        verify(preparedStatement).setObject(1, targetName);
        assertAll(
                () -> assertThat(testUsers.size()).isEqualTo(2),
                () -> assertThat(testUsers).containsExactlyInAnyOrder(new TestUser(1L, "pola"), new TestUser(2L, "pola"))
        );
    }

    @DisplayName("전달받은 sql과 파라미터의 조건에 맞는 객체를 반환한다.")
    @Test
    void queryForObject() throws SQLException {
        //given
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong(1)).thenReturn(1L);
        when(resultSet.getString(2)).thenReturn("pola");
        String sql = "select id, name from test_users where id = ?";
        Long targetId = 1L;

        // when
        TestUser found = jdbcTemplate.queryForObject(sql, getRowMapper(), targetId);

        // then
        verify(preparedStatement).setObject(1, targetId);
        assertThat(found).isEqualTo(new TestUser(1L, "pola"));
    }

    @DisplayName("전달받은 sql과 파라미터의 조건에 맞는 객체가 없으면 예외를 발생시킨다.")
    @Test
    void queryForObjectEmpty() throws SQLException {
        //given
        when(resultSet.next()).thenReturn(false);
        String sql = "select id, name from test_users where id = ?";
        Long targetId = 1L;

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, getRowMapper(), targetId))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("일치하는 데이터가 없습니다.");
    }

    @DisplayName("전달받은 sql과 파라미터의 조건에 맞는 객체가 2개 이상이면 예외를 발생시킨다.")
    @Test
    void queryForObjectTooManyResults() throws SQLException {
        //given
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong(1)).thenReturn(1L, 2L);
        when(resultSet.getString(2)).thenReturn("pola", "pola");
        String sql = "select id, name from test_users where name = ?";
        String targetName = "pola";

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, getRowMapper(), targetName))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("일치하는 데이터가 2개 이상입니다.");
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
