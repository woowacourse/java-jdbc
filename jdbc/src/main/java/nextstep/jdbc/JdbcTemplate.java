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

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return executeUpdate(connection -> makePreparedStatement(sql, connection, args));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeQueryForObject(connection -> makePreparedStatement(sql, connection, args), rowMapper);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeQuery(connection -> makePreparedStatement(sql, connection, args), rowMapper);
    }

    private PreparedStatement makePreparedStatement(String sql, Connection connection, Object... args) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
        return preparedStatement;
    }

    private int executeUpdate(StatementStrategy stmt) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = stmt.makePreparedStatement(conn)) {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    private <T> T executeQueryForObject(StatementStrategy stmt, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = stmt.makePreparedStatement(conn);
             ResultSet rs = preparedStatement.executeQuery()) {
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    private <T> List<T> executeQuery(StatementStrategy stmt, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = stmt.makePreparedStatement(conn);
             ResultSet rs = preparedStatement.executeQuery()) {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }
}
