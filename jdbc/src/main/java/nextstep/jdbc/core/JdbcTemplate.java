package nextstep.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.dao.DataAccessException;
import nextstep.jdbc.support.DataAccessUtils;
import nextstep.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        Assert.notNull(sql, "SQL must not be null");
        Assert.notNull(rowMapper, "RowMapper must not be null");

        return execute(sql, (preparedStatement -> {
            var resultSet = preparedStatement.executeQuery();
            return toRows(rowMapper, resultSet);
        }));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        Assert.notNull(sql, "SQL must not be null");
        Assert.notNull(rowMapper, "RowMapper must not be null");

        return execute(sql, (preparedStatement -> {
            setParams(preparedStatement, args);
            var resultSet = preparedStatement.executeQuery();
            return DataAccessUtils.singleResult(toRows(rowMapper, resultSet));
        }));
    }

    private <T> List<T> toRows(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        var rows = new ArrayList<T>();
        while (resultSet.next()) {
            rows.add(rowMapper.mapRow(resultSet));
        }
        return rows;
    }

    public void update(final String sql, final Object... args) {
        Assert.notNull(sql, "SQL must not be null");

        execute(sql, preparedStatement -> {
            setParams(preparedStatement, args);
            return preparedStatement.executeUpdate();
        });
    }

    private void setParams(final PreparedStatement preparedStatement, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    public void execute(final String sql) {
        execute(sql, PreparedStatement::execute);
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> action) {
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement(sql)) {
            return action.doInPreparedStatement(preparedStatement);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
