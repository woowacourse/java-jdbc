package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class JdbcTemplate {

    private final PreparedStatementExecuteTemplate preparedStatementExecuteTemplate;
    private final ResultSetMappingTemplate resultSetMappingTemplate;

    public JdbcTemplate(final DataSource dataSource) {
        this.preparedStatementExecuteTemplate = new PreparedStatementExecuteTemplate(dataSource);
        this.resultSetMappingTemplate = new ResultSetMappingTemplate();
    }

    public int update(final String sql, final Object... args) {
        return preparedStatementExecuteTemplate.execute(PreparedStatement::executeUpdate, sql, args);
    }

    public int update(final Connection conn, final String sql, final Object... args) {
        return preparedStatementExecuteTemplate.execute(conn, PreparedStatement::executeUpdate, sql, args);
    }

    public <T> T queryObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return preparedStatementExecuteTemplate.execute(pstmt -> {
            final List<T> queriedData = resultSetMappingTemplate.mapping(pstmt, rowMapper);
            if (queriedData.size() > 1) {
                throw new DataAccessException("조회한 데이터가 하나보다 더 많이 존재합니다.");
            }

            return queriedData.get(0);
        }, sql, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return preparedStatementExecuteTemplate.execute(pstmt -> resultSetMappingTemplate.mapping(pstmt, rowMapper), sql);
    }
}
