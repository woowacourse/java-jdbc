package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("queryForObject의 결과가 존재하지 않으면 예외가 발생한다.")
    void queryForObjectTestWithNoResult() throws SQLException {
        String sql = "SELECT * FROM user WHERE name = ?";
        AtomicLong id = new AtomicLong(1L);
        RowMapper<User> rowMapper = rs -> new User(id.getAndIncrement(), "naknak");

        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, "naknak"))
                .isInstanceOf(DataAccessException.class)
                .hasRootCauseMessage("쿼리 실행 결과가 1개이기를 기대했지만, 0개입니다.");
    }

    @Test
    @DisplayName("queryForObject의 결과가 2개 이상이면 예외가 발생한다.")
    void queryForObjectTestWithMoreResults() throws SQLException {
        String sql = "SELECT * FROM user WHERE name = ?";
        AtomicLong id = new AtomicLong(1L);
        RowMapper<User> rowMapper = rs -> new User(id.getAndIncrement(), "naknak");

        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, "naknak"))
                .isInstanceOf(DataAccessException.class)
                .hasRootCauseMessage("쿼리 실행 결과가 1개이기를 기대했지만, 2개 이상입니다.");
    }

    @Test
    @DisplayName("queryForObject다 결과가 1개면 정상적으로 객체를 반환한.")
    void queryForObjectTest() throws SQLException {
        String sql = "SELECT * FROM user WHERE name = ?";
        AtomicLong id = new AtomicLong(1L);
        RowMapper<User> rowMapper = rs -> new User(id.getAndIncrement(), "naknak");

        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(false);

        User user = jdbcTemplate.queryForObject(sql, rowMapper, "naknak");

        assertAll(
                () -> assertThat(user.getId()).isEqualTo(1L),
                () -> assertThat(user.getName()).isEqualTo("naknak")
        );
    }


    static class User {
        private final Long id;
        private final String name;

        public User(Long id, String name) {
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
