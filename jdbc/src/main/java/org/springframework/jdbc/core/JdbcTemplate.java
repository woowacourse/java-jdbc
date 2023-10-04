package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplateException.DatabaseAccessException;
import org.springframework.jdbc.core.JdbcTemplateException.MoreDataAccessException;
import org.springframework.jdbc.core.JdbcTemplateException.NoDataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final Object... fields) {
        log.debug("query : {}", sql);

        execute(sql, PreparedStatement::executeUpdate, fields);
    }

    private void setPreparedStatement(final PreparedStatement ps, final Object[] fields) throws SQLException {
        for (int i = 1; i <= fields.length; i++) {
            ps.setObject(i, fields[i - 1]);
        }
    }

    private <T> T execute(final String sql,
                          final PrepareStatementExecutor<T> executor,
                          final Object... fields) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            setPreparedStatement(ps, fields);

            return executor.execute(ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DatabaseAccessException(e.getMessage());
        }
    }

    public <T> T find(final String sql,
                      final RowMapper<T> rowMapper,
                      final Object... fields) {
        log.debug("query : {}", sql);

        return execute(sql,
                ps -> executeResultSet(rowMapper, ps, (rm, rs) -> List.of(getObject(rm, rs))).get(0),
                fields);
    }

    private <T> List<T> executeResultSet(final RowMapper<T> rowMapper,
                                         final PreparedStatement ps,
                                         final ResultSetExecutor<T> resultSetExecutor) throws SQLException {
        try (final ResultSet resultSet = ps.executeQuery()) {
            return resultSetExecutor.execute(rowMapper, resultSet);
        }
    }

    private <T> T getObject(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        if (rs.next() && rs.isLast()) {
            return rowMapper.mapRow(rs);
        }
        if (!rs.next()) {
            throw new NoDataAccessException();
        }
        throw new MoreDataAccessException();
    }

    public <T> List<T> findAll(final String sql, final RowMapper<T> rowMapper) {
        log.debug("query : {}", sql);

        return execute(sql, ps -> executeResultSet(rowMapper, ps, this::getObjects));
    }

    private <T> List<T> getObjects(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        final List<T> objects = new ArrayList<>();
        while (rs.next()) {
            objects.add(rowMapper.mapRow(rs));
        }
        return objects;
    }
}
