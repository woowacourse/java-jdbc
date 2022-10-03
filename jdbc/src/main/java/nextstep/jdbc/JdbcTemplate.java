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

    public void update(final String sql, final Object... args) {
        execute(sql, pstmt -> setParameters(pstmt, args).executeUpdate());
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> {
            setParameters(pstmt, args);
            final ResultSet resultSet = pstmt.executeQuery();
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        });
    }


    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> {
            setParameters(pstmt, args);
            final ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            throw new DataAccessException("잘못된 결과입니다.");
        });
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> preparedStatementCallback) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            return preparedStatementCallback.doPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private PreparedStatement setParameters(final PreparedStatement preparedStatement, final Object... args)
            throws SQLException {

        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
        return preparedStatement;
    }

}
