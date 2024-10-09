package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<Long> ROW_MAPPER = rs -> rs.getLong("id");

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.connection = mock(Connection.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @AfterEach
    void tearDown() throws SQLException {
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    void write_쿼리를_실행한다() throws SQLException {
        // when
        jdbcTemplate.update("write query");

        // then
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void read_쿼리로_n개의_데이터를_조회한다() throws SQLException {
        // given
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // when
        List<Long> results = jdbcTemplate.query("read query", ROW_MAPPER);

        // then
        assertAll(
                () -> verify(preparedStatement).executeQuery(),
                () -> assertThat(results).hasSize(2),
                () -> verify(resultSet).close()
        );
    }

    @Test
    void read_쿼리로_한_개의_데이터를_조회한다() throws SQLException {
        // given
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // when
        Optional<Long> result = jdbcTemplate.queryForObject("read query", ROW_MAPPER);

        // then
        assertAll(
                () -> verify(preparedStatement).executeQuery(),
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get()).isEqualTo(1L),
                () -> verify(resultSet).close()
        );
    }

    @Test
    void read_쿼리로_한_개의_데이터_조회_시_데이터가_없으면_빈_값을_반환한다() throws SQLException {
        // give
        when(resultSet.next()).thenReturn(false);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // when
        Optional<Long> result = jdbcTemplate.queryForObject("read query", ROW_MAPPER);

        // then
        assertAll(
                () -> verify(preparedStatement).executeQuery(),
                () -> assertThat(result).isEmpty(),
                () -> verify(resultSet).close()
        );
    }

    @Test
    void read_쿼리로_한_개의_데이터_조회_시_데이터가_2개_이상이면_예외가_발생한다() throws SQLException {
        // given
        when(resultSet.next()).thenReturn(true, true, false);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject("read query", ROW_MAPPER, 1L))
                .isExactlyInstanceOf(IncorrectResultSizeDataAccessException.class)
                .hasMessage("Incorrect result size. expected size: 1, actual size: 2");
    }
}
