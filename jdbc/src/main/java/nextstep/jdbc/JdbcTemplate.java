package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public int update(final String sql, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParameters(preparedStatement, args);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql);
             final ResultSet resultSet = createResultSet(preparedStatement, args)) {
            log.debug("query : {}", sql);

            List<T> values = new ArrayList<>();
            while (resultSet.next()) {
                values.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
            }
            return values;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private ResultSet createResultSet(final PreparedStatement preparedStatement, final Object... args) throws SQLException {
        setParameters(preparedStatement, args);
        return preparedStatement.executeQuery();
    }

    private void setParameters(final PreparedStatement pstmt, final Object... args) {
        for (int i = 0; i < args.length; i++) {
            try {
                pstmt.setObject(i + 1, args[i]);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
