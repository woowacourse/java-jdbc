package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int PREPARED_STATEMENT_START_INDEX = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql,
                                final RowMapper<T> rowMapper,
                                final Object... args) {
        return connect(sql, pstmt -> {
            try (final var resultSet = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                for (int i = 0; resultSet.next(); i++) {
                    result.add(rowMapper.mapRow(resultSet, i));
                }

                return result.get(0);
            } catch (NullPointerException e) {
                return null;
            }
        }, args);
    }

    public <T> List<T> queryForList(final String sql,
                                    final RowMapper<T> rowMapper,
                                    final Object... args) {
        return connect(sql, pstmt -> {
            try (final var resultSet = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                for (int i = 0; resultSet.next(); i++) {
                    result.add(rowMapper.mapRow(resultSet, i));
                }

                return result;
            } catch (NullPointerException e) {
                return new ArrayList<>();
            }
        }, args);
    }

    public int update(final String sql,
                      final Object... args) {
        return connect(sql, PreparedStatement::executeUpdate, args);
    }

    private <T> T connect(final String sql,
                          final ExecuteStrategy<T> strategy,
                          final Object... args) {
        try (
            final var connection = dataSource.getConnection();
            final var preparedStatement = connection.prepareStatement(sql)) {

            for (int i = PREPARED_STATEMENT_START_INDEX; i < args.length; i++) {
                preparedStatement.setObject(i, args[i - 1]);
            }
            return strategy.execute(preparedStatement);
        } catch (SQLException e) {
            log.error("Not Data Access : {}", e.getMessage());
            throw new DataAccessException(e);
        }
    }

    interface ExecuteStrategy<T> {
        T execute(final PreparedStatement pstmt) throws SQLException;
    }
}
