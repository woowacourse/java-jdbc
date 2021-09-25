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

    private final DataSource dataSource;

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTemplate.class);

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        LOGGER.info("sql query: {}", sql);
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            executeUpdate(preparedStatement, args);
        } catch (SQLException exception) {
            LOGGER.debug("exception occurred while sql execute");
            LOGGER.debug("exception message: {}", exception.getMessage());
        }
    }

    private void executeUpdate(final PreparedStatement preparedStatement, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; ++i) {
            preparedStatement.setObject(i + 1, args[i]);
        }
        preparedStatement.executeUpdate();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> userRowMapper) {
        LOGGER.info("sql query: {}", sql);
        List<T> queryResults = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
        ) {
            int rowNumber = 1;
            while (rs.next()) {
                queryResults.add(userRowMapper.mapRow(rs, rowNumber++));
            }
            return queryResults;
        } catch (SQLException exception) {
            LOGGER.debug("exception occurred while sql execute");
            LOGGER.debug("exception message: {}", exception.getMessage());
            return queryResults;
        }
    }
}
