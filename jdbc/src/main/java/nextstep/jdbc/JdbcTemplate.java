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

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return execute(PreparedStatement::executeUpdate, sql, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);

        if (results.isEmpty()) {
            throw new DataAccessException("데이터 조회에 실패했습니다.");
        }

        if (results.size() > 1) {
            throw new DataAccessException("데이터 조회 결과가 2개 이상입니다.");
        }

        return results.iterator().next();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(preparedStatement -> executeQueryAndMappingList(preparedStatement, rowMapper), sql, args);
    }

    private <T> List<T> executeQueryAndMappingList(PreparedStatement preparedStatement, RowMapper<T> rowMapper) {
        try (ResultSet resultSet = preparedStatement.executeQuery()){
            List<T> results = new ArrayList<>();

            while (resultSet.next()) {
                results.add(rowMapper.map(resultSet));
            }

            return results;
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    private <T> T execute(PreparedStatementCallback<T> preparedStatementCallback, String sql, Object... args) {
        LOG.debug("query : {}", sql);

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = createStatement(connection, sql, args)
        ) {
            return preparedStatementCallback.call(preparedStatement);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    private PreparedStatement createStatement(Connection connection, String sql, Object[] args) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        int argumentsCount = args.length;
        for (int i = 0; i < argumentsCount; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }

        return preparedStatement;
    }
}
