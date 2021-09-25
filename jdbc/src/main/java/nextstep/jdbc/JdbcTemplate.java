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

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return execute(PreparedStatement::executeUpdate, sql, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(preparedStatement -> executeQueryAndMappingList(preparedStatement, rowMapper), sql, args);
    }

    private <T> List<T> executeQueryAndMappingList(PreparedStatement preparedStatement, RowMapper<T> rowMapper) {
        try (ResultSet rs = preparedStatement.executeQuery()){
            List<T> results = new ArrayList<>();

            while (rs.next()) {
                results.add(rowMapper.map(rs));
            }

            return results;
        } catch (SQLException e){
            throw new RuntimeException();
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(preparedStatement -> executeQueryAndMapping(preparedStatement, rowMapper), sql, args);
    }

    private <T> T executeQueryAndMapping(PreparedStatement preparedStatement, RowMapper<T> rowMapper) {
        try (ResultSet rs = preparedStatement.executeQuery()){
            if (rs.next()) {
                return rowMapper.map(rs);
            } else {
                throw new SQLException();
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    private <T> T execute(StatementCallback<T> statementCallback, String sql, Object... args) {
        LOG.debug("query : {}", sql);

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = createStatement(connection, sql, args)
        ) {
            return statementCallback.call(statement);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement createStatement(Connection connection, String sql, Object[] args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);

        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }

        return statement;
    }
}
