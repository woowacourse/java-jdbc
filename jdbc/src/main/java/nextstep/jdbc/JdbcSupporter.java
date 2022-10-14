package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcSupporter {

    private static final Logger log = LoggerFactory.getLogger(JdbcSupporter.class);

    private final DataSource dataSource;

    public JdbcSupporter(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    <T> T execute(final String sql, final JdbcExecutor<T> executor, final Object... args) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setParams(statement, args);
            log.debug("query : {}", sql);
            return executor.execute(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void setParams(final PreparedStatement statement, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    protected DataSource getDataSource() {
        return this.dataSource;
    }
}
