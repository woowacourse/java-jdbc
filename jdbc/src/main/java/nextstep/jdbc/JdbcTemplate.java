package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import nextstep.jdbc.support.IntConsumerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final Connection connection,
                      final String sql,
                      final Object... args) {
        return execute(connection, sql, PreparedStatement::executeUpdate, args);
    }

    public int update(final String sql,
                      final Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(final String sql,
                                final RowMapper<T> rowMapper,
                                final Object... args) {

        return execute(sql, pstmt -> {
            List<T> result = mapToObjects(rowMapper, pstmt);

            return DataAccessUtils.nullableSingleResult(result);
        }, args);
    }

    public <T> List<T> queryForList(final String sql,
                                    final RowMapper<T> rowMapper,
                                    final Object... args) {
        return execute(sql, pstmt -> mapToObjects(rowMapper, pstmt), args);
    }

    private <T> List<T> mapToObjects(final RowMapper<T> rowMapper, final PreparedStatement pstmt) {
        try (final var resultSet = pstmt.executeQuery()) {
            List<T> result = new ArrayList<>();
            for (int i = 0; resultSet.next(); i++) {
                result.add(rowMapper.mapRow(resultSet, i));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Query Exception", e);
        }
    }

    private <T> T execute(final Connection connection,
                          final String sql,
                          final StatementCallBack<T> statement,
                          final Object... args) {
        PreparedStatementCreator statementCreator = preparedStatementCreator(args);

        try (final var preparedStatement = statementCreator.create(connection, sql)) {

            return statement.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to Access DataBase", e);
        }
    }

    private <T> T execute(final String sql,
                          final StatementCallBack<T> statement,
                          final Object... args) {
        PreparedStatementCreator statementCreator = preparedStatementCreator(args);

        try (
            final var connection = dataSource.getConnection();
            final var preparedStatement = statementCreator.create(connection, sql)) {

            return statement.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to Access DataBase", e);
        }
    }

    private PreparedStatementCreator preparedStatementCreator(final Object... args) {
        return (connection, sql) -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            IntStream.range(0, args.length)
                .forEach(
                    IntConsumerWrapper.accept(index -> pstmt.setObject(index + 1, args[index])));

            return pstmt;
        };
    }
}
