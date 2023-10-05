package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

abstract class AutoClosableTemplate {

    private static final Logger log = LoggerFactory.getLogger(AutoClosableTemplate.class);

    private final DataSource dataSource;

    AutoClosableTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final Object... objects) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = preparedStatementAndSetValue(conn, sql, objects)
        ) {
            commandQuery(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = preparedStatementAndSetValue(conn, sql, objects)
        ) {
            final ResultSet rs = pstmt.executeQuery();
            return queryAll(rs, rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Nullable
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = preparedStatementAndSetValue(conn, sql, objects)
        ) {
            final ResultSet rs = pstmt.executeQuery();
            return queryForOne(rs, rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement preparedStatementAndSetValue(
            final Connection conn,
            final String sql,
            final Object... objects
    ) throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        log.debug("query : {}", sql);
        setValues(pstmt, objects);
        return pstmt;
    }

    private void setValues(final PreparedStatement pstmt, final Object... objects) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            pstmt.setObject(i + 1, objects[i]);
        }
    }

    protected abstract void commandQuery(final PreparedStatement pstmt) throws SQLException;

    protected abstract <T> List<T> queryAll(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException;

    @Nullable
    protected abstract <T> T queryForOne(final ResultSet rs, final RowMapper<T> rowMapper)
            throws SQLException;
}
