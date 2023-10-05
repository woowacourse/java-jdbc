package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.RowMapper;
import org.springframework.exception.EmptyResultException;
import org.springframework.exception.WrongResultSizeException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final int SINGLE_RESULT_SIZE = 1;

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final PreparedStatementExecutor preparedStatementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        preparedStatementExecutor = new PreparedStatementExecutor(dataSource);
    }

    public void update(final String sql, final Object... parameters) {
        preparedStatementExecutor.execute(
                generatePreparedStatement(sql, parameters),
                PreparedStatement::executeUpdate
        );
    }

    private PreparedStatementGenerator generatePreparedStatement(final String sql, final Object... parameters) {
        return connection -> {
            log.debug("query : {}", sql);
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            setParameters(preparedStatement, parameters);
            return preparedStatement;
        };
    }

    private void setParameters(final PreparedStatement pstmt, final Object[] parameters) throws SQLException {
        for (int i = 1; i <= parameters.length; i++) {
            pstmt.setObject(i, parameters[i - 1]);
        }
    }

    public <T> List<T> query(final RowMapper<T> rowMapper, final String sql, final Object... parameters) {
        return preparedStatementExecutor.execute(
                generatePreparedStatement(sql, parameters),
                preparedStatement -> findQueryResults(rowMapper, preparedStatement)
        );
    }

    private <T> List<T> findQueryResults(final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException {
        final ResultSet rs = pstmt.executeQuery();

        final List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }
        return results;
    }

    public <T> T queryForObject(final RowMapper<T> rowMapper, final String sql, final Object... parameters) {
        final List<T> result = query(rowMapper, sql, parameters);

        validateResultSize(result.size());
        return result.get(0);
    }

    private static <T> void validateResultSize(final int size) {
        if (size > SINGLE_RESULT_SIZE) {
            throw new WrongResultSizeException("Result Count is Not Only 1. ResultCount=" + size);
        }
        if (size == 0) {
            throw new EmptyResultException("Result is Empty");
        }
    }
}
