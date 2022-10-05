package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int ROW_NUM = 0;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... parameters) {
        validateSql(sql);
        query(sql, (PreparedStatement statement) -> {
            setPreparedStatement(parameters, statement);
            statement.executeUpdate();
            return null;
        });
    }

    public <T> List<T> select(final String sql, final ObjectMapper<T> objectMapper, final Object... parameters) {
        validateSql(sql);
        return queryForList(sql, (PreparedStatement statement) -> {
            setPreparedStatement(parameters, statement);
            final ResultSet resultSet = statement.executeQuery();

            return getResults(objectMapper, resultSet);
        });
    }

    private void validateSql(final String sql) {
        if (sql == null) {
            throw new DataAccessException("sql must not be null");
        }
    }

    private void setPreparedStatement(final Object[] parameters, final PreparedStatement statement) throws
        SQLException {
        for (int i = ROW_NUM; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }

    private <T> List<T> getResults(final ObjectMapper<T> objectMapper, final ResultSet resultSet) throws SQLException {
        final List<T> mappedTypes = new ArrayList<>();
        while (resultSet.next()) {
            mappedTypes.add(objectMapper.mapObject(resultSet, ROW_NUM));
        }
        return mappedTypes;
    }

    private <T> void query(final String sql, final Executable<T> executable) {
        queryForList(sql, executable);
    }

    private <T> List<T> queryForList(final String sql, final Executable<T> executable) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            return executable.execute(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
