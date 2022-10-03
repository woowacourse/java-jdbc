package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        log.debug("query : {}", sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParams(preparedStatement, params);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setParams(final PreparedStatement preparedStatement, final Object[] params) throws SQLException {
        int size = params.length;
        for (int i = 0; i < size; i++) {
            setObject(preparedStatement, i + 1, params[i]);
        }
    }

    private void setObject(final PreparedStatement preparedStatement, final int index, final Object param)
            throws SQLException {
        if (param instanceof String) {
            preparedStatement.setString(index, (String) param);
            return;
        }

        if (param instanceof Long) {
            preparedStatement.setLong(index, (Long) param);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
