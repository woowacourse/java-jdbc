package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... arguments) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)) {

            setObjectToPreparedStatement(pstmt, arguments);

            return getObject(sql, rowMapper, pstmt);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setObjectToPreparedStatement(final PreparedStatement pstmt, final Object[] arguments) throws SQLException {
        for (int parameterIndex = 1; parameterIndex < arguments.length + 1; parameterIndex++) {
            pstmt.setObject(parameterIndex, arguments[parameterIndex - 1]);
        }
    }

    private <T> Optional<T> getObject(final String sql, final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException {
        try (final ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);
            if (!rs.next()) {
                return Optional.empty();
            }
            final T object = rowMapper.map(rs);
            if (rs.next()) {
                throw new SQLException("Query returned more than one result");
            }
            return Optional.of(object);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql);
             final var rs = pstmt.executeQuery();) {

            log.debug("query : {}", sql);

            return getObjects(rowMapper, rs);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> getObjects(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        final List<T> result = new ArrayList<>();
        while (rs.next()) {
            final T data = rowMapper.map(rs);
            result.add(data);
        }
        return result;
    }

    public void update(final String sql, final Object... arguments) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql);) {

            log.debug("query : {}", sql);

            setObjectToPreparedStatement(pstmt, arguments);

            pstmt.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
