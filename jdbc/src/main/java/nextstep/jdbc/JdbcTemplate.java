package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.resultset.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

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

            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            return strategy.execute(preparedStatement);
        } catch (SQLException e) {
            log.error("Not Connect : {}", e.getMessage());
            throw new DataAccessException(e);
        }
    }

    interface ExecuteStrategy<T> {
        T execute(final PreparedStatement pstmt) throws SQLException;
    }
}
