package org.springframework.jdbc.core;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public <T> T queryObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return preparedStatementExecuteTemplate.execute(pstmt -> {
            final ResultSet rs = pstmt.executeQuery();

            return resultSetMappingTemplate.mappingOne(rs, rowMapper);
        }, sql, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return preparedStatementExecuteTemplate.execute(pstmt -> {
            final ResultSet rs = pstmt.executeQuery();

            return resultSetMappingTemplate.mapping(rs, rowMapper);
        }, sql);
    }
}
