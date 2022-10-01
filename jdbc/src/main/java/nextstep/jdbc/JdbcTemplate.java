package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final Map<Integer, Object> params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            setParams(statement, params);
            statement.execute();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T query(final String sql, final Map<Integer, Object> params, final Function<ResultSet, T> function) {
        ResultSet resultSet = null;
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            setParams(statement, params);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return function.apply(resultSet);
            }
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (final SQLException ignored) {
            }
        }
        return null;
    }

    private void setParams(final PreparedStatement statement, final Map<Integer, Object> params)
            throws SQLException {
        for (final Entry<Integer, Object> param : params.entrySet()) {
            statement.setObject(param.getKey(), param.getValue());
        }
    }
}
