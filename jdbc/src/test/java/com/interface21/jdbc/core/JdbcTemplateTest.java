package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    @Test
    @DisplayName("단건 조회 결과가 존재하지 않는 경우 null을 반환한다.")
    void emptyQueryOne() throws SQLException {
        DataSource source = getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(source);

        Object result = jdbcTemplate.queryOne("sql", (rs) -> new Object());

        assertThat(result).isEqualTo(null);
    }

    @Test
    @DisplayName("여러건 데이터 조회 결과가 존재하지 않는 경우 null을 반환한다.")
    void emptyQuery() throws SQLException {
        DataSource source = getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(source);

        List<Object> result = jdbcTemplate.query("sql", (rs) -> new Object());

        assertThat(result).isEmpty();
    }

    private DataSource getDataSource() throws SQLException {
        DataSource source = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(source.getConnection())
                .thenReturn(conn);
        when(conn.prepareStatement(any()))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        when(resultSet.next())
                .thenReturn(false);

        return source;
    }
}
