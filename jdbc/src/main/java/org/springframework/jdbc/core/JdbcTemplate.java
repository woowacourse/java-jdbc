package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.exception.DataNotFoundException;
import org.springframework.jdbc.core.exception.JdbcTemplateException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeUpdate(final String sql, final Object... parameters) {
        executeSql(PreparedStatement::execute, sql, parameters);
    }

    public <T> T executeQuery(
        final String sql,
        final Mapper<T> mapper,
        final Object... objects
    ) {
        return executeSql(pst -> {
            final ResultSet rs = pst.executeQuery();
            log.debug("query : {}", sql);
            if (rs.next()) {
                return mapper.map(rs);
            }
            throw new DataNotFoundException();
        }, sql, objects);
    }

    private void setPreparedStatement(
        final PreparedStatement pst,
        final Object[] parameters
    ) throws SQLException {
        for (int index = 1; index <= parameters.length; index++) {
            pst.setObject(index, parameters[index - 1]);
        }
    }

    private PreparedStatement createPreparedStatement(
        final String sql,
        final Connection connection,
        final Object... parameters
    ) throws SQLException {
        final PreparedStatement pst = connection.prepareStatement(sql);
        setPreparedStatement(pst, parameters);
        return pst;
    }

    private <T> T executeSql(
        final PreparedStatementExecutor<T> preparedStatementExecutor,
        final String sql,
        final Object... objects) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement pst = createPreparedStatement(sql, connection, objects)
        ) {
            return preparedStatementExecutor.execute(pst);
        } catch (final SQLException e) {
            throw new JdbcTemplateException(e);
        }
    }
}
