package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
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
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            setPreparedStatementWithArgs(preparedStatement, args);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            LOGGER.debug("exception occurred while sql execute");
            LOGGER.debug("exception message: {}", exception.getMessage());
            throw new DataAccessException();
        }
    }

    private void setPreparedStatementWithArgs(final PreparedStatement preparedStatement, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; ++i) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        LOGGER.info("sql query: {}", sql);
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            setPreparedStatementWithArgs(preparedStatement, args);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                List<T> queryResults = new ArrayList<>();
                int rowNumber = 1;
                while (rs.next()) {
                    queryResults.add(rowMapper.mapRow(rs, rowNumber++));
                }
                return queryResults;
            }
        } catch (SQLException exception) {
            LOGGER.debug("exception occurred while sql execute");
            LOGGER.debug("exception message: {}", exception.getMessage());
            throw new DataAccessException();
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> userRowMapper, final Object... args) {
        List<T> results = query(sql, userRowMapper, args);
        validateQueryForObject(results);
        return results.get(0);
    }

    private <T> void validateQueryForObject(final List<T> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("찾는 데이터가 없습니다.");
        }
    }
}
