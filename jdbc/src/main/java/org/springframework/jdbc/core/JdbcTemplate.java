package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.exception.InvalidDataSizeException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int FIRST_INDEX_OF_RESULT = 0;
    private static final int MIN_RESULT_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String query, final Object... columns) {
        executeUpdate(query, columns);
    }

    public <T> List<T> query(final String query, final RowMapper<T> rowMapper, final Object... columns) {
        return executeQuery(query, rowMapper, columns);
    }

    public <T> Optional<T> queryForObject(final String query, final RowMapper<T> rowMapper, final Object... columns) {
        final List<T> result = executeQuery(query, rowMapper, columns);
        validateSize(result);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.get(FIRST_INDEX_OF_RESULT));
    }

    private void executeUpdate(final String query, final Object[] columns) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = getPreparedstatement(conn, query, columns);
        ) {
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> executeQuery(final String query, final RowMapper<T> rowMapper, final Object... columns) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = getPreparedstatement(conn, query, columns);
                final ResultSet rs = pstmt.executeQuery()
        ) {
            return getResult(rowMapper, rs);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement getPreparedstatement(final Connection conn, final String query, final Object[] columns)
            throws SQLException {
        final PreparedStatement preparedStatement = conn.prepareStatement(query);
        setParameters(preparedStatement, columns);
        return preparedStatement;
    }

    private void setParameters(final PreparedStatement pstmt, final Object[] columns) throws SQLException {
        for (int i = 0; i < columns.length; i++) {
            pstmt.setObject(i + 1, columns[i]);
        }
    }

    private <T> List<T> getResult(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        final List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.map(rs));
        }
        return result;
    }

    private <T> void validateSize(final List<T> result) {
        if (result.size() > MIN_RESULT_SIZE) {
            throw new InvalidDataSizeException("결과가 1건 이상 조회되었습니다.");
        }
    }
}
