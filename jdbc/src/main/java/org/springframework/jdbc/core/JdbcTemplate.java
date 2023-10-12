package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... parameters) {
        return executeQuery(sql, PreparedStatement::executeUpdate, parameters);
    }

    private <T> T executeQuery(final String sql, final PreparedStateExecutor<T> executor, final Object... parameters) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = PreparedStateUtil.makeStatement(conn, sql, parameters)) {
            log.debug("Execute - query : {}", sql);
            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return executeQuery(sql, preparedStatement -> SingleResult.makeSingleResultFrom(makeResults(rowMapper, preparedStatement)), parameters);
    }

    private <T> List<T> makeResults(final RowMapper<T> rowMapper, final PreparedStatement preparedStatement) throws SQLException {
        List<T> list = new ArrayList<>();
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                list.add(rowMapper.mapping(resultSet));
            }
        }
        return list;
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return executeQuery(sql, preparedStatement -> makeResults(rowMapper, preparedStatement), parameters);
    }
}
