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

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return connect(sql, (PreparedStatement preparedStatement) -> {
            setArgsToPreparedStatement(preparedStatement, args);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return connect(sql, (PreparedStatement preparedStatement) -> {
            setArgsToPreparedStatement(preparedStatement, args);
            return executeGetObjectQuery(rowMapper, preparedStatement);
        });
    }

    private <T> T executeGetObjectQuery(final RowMapper<T> rowMapper,
                                        final PreparedStatement preparedStatement) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            throw new RuntimeException("결과가 존재하지 않습니다.");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return connect(sql, (PreparedStatement preparedStatement) -> {
            setArgsToPreparedStatement(preparedStatement, args);
            return executeGetListQuery(rowMapper, preparedStatement);
        });
    }

    private <T> ArrayList<T> executeGetListQuery(final RowMapper<T> rowMapper,
                                                 final PreparedStatement preparedStatement) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            ArrayList<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private  <T> T connect(final String sql, final Executor<T> executor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return executor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setArgsToPreparedStatement(final PreparedStatement preparedStatement, final Object[] args)
            throws SQLException {
        for (int idx = 0; idx < args.length; idx++) {
            preparedStatement.setObject(idx + 1, args[idx]);
        }
    }
}
