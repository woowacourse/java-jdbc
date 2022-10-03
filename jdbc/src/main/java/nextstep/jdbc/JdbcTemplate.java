package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        Assert.notNull(sql, "SQL must not be null");
        log.debug("execute prepared SQL update");

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setValues(statement, args);
            statement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setValues(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        Assert.notNull(sql, "SQL must not be null");
        log.debug("Executing SQL query [{}]", sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            setValues(statement, args);
            final ResultSet resultSet = statement.executeQuery();
            return extractData(resultSet, rowMapper);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T extractData(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        Assert.notNull(rowMapper, "RowMapper is required");
        if (!resultSet.next()) {
            return null;
        }
        return rowMapper.mapRow(resultSet, 0);
    }
}
