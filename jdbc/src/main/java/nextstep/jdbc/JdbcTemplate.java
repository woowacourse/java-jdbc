package nextstep.jdbc;

import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return executeUpdate(PreparedStatementCreatorFactory.create(sql, args));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeQuery(
                PreparedStatementCreatorFactory.create(sql, args),
                ResultSetExtractorFactory.objectResultSetExtractor(rowMapper)
        );
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeQuery(
                PreparedStatementCreatorFactory.create(sql, args),
                ResultSetExtractorFactory.listResultSetExtractor(rowMapper)
        );
    }

    private int executeUpdate(PreparedStatementCreator stmt) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = stmt.makePreparedStatement(conn)) {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    private <T> T executeQuery(PreparedStatementCreator stmt, ResultSetExtractor<T> rse) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = stmt.makePreparedStatement(conn);
             ResultSet rs = preparedStatement.executeQuery()) {
            return rse.extract(rs);
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }
}
