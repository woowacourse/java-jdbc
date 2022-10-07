package nextstep.jdbc;

import nextstep.jdbc.exception.*;
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

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, preparedStatement -> {
            log.debug("query : {}", sql);
            setParameters(preparedStatement, args);
            return update(preparedStatement);
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> result = query(sql, rowMapper, args);
        if (result.isEmpty()) {
            throw new EmptyResultException();
        }
        if (result.size() > 1) {
            throw new IncorrectResultSizeException();
        }

        return result.iterator().next();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, preparedStatement -> {
            setParameters(preparedStatement, args);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                log.debug("query : {}", sql);

                final List<T> values = new ArrayList<>();
                while (resultSet.next()) {
                    values.add(rowMapper.mapRow(resultSet));
                }
                return values;
            }
        });
    }

    private void setParameters(final PreparedStatement preparedStatement, final Object... args) {
        for (int i = 0; i < args.length; i++) {
            try {
                preparedStatement.setObject(i + 1, args[i]);
            } catch (final SQLException e) {
                log.error(e.getMessage(), e);
                throw new ParameterBindingException();
            }
        }
    }

    private <R> R execute(final String sql, final PreparedStatementCallback<R> action) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return action.doInStatement(preparedStatement);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("데이터 접근에 실패했습니다.");
        }
    }

    private int update(final PreparedStatement preparedStatement) {
        try {
            return preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new InvalidStatementException();
        }
    }
}
