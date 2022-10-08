package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql,
                      final Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(final String sql,
                                final RowMapper<T> rowMapper,
                                final Object... args) {
        return execute(sql, pstmt -> {
            try (final var resultSet = pstmt.executeQuery()) {
                List<T> result = mapToObjects(rowMapper, resultSet);

                return result.get(0);
            } catch (SQLException | IndexOutOfBoundsException e) {
                log.error(e.getMessage(), e);
                throw new DataAccessException(e);
            }
        }, args);
    }

    public <T> List<T> queryForList(final String sql,
                                    final RowMapper<T> rowMapper,
                                    final Object... args) {
        return execute(sql, pstmt -> {
            try (final var resultSet = pstmt.executeQuery()) {
                List<T> result = mapToObjects(rowMapper, resultSet);

                return result;
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataAccessException(e);
            }
        }, args);
    }

    private <T> List<T> mapToObjects(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();
        for (int i = 0; resultSet.next(); i++) {
            result.add(rowMapper.mapRow(resultSet, i));
        }

        return result;
    }

    private <T> T execute(final String sql,
                          final ExecuteStrategy<T> strategy,
                          final Object... args) {
        PreparedStatementCreator statementCreator = preparedStatementCreator(args);

        try (
            final var connection = dataSource.getConnection();
            final var preparedStatement = statementCreator.create(connection, sql)) {

            return strategy.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to Access DataBase", e);
        }
    }

    private PreparedStatementCreator preparedStatementCreator(final Object... args) {
        return (connection, sql) -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            return pstmt;
        };
    }
}
