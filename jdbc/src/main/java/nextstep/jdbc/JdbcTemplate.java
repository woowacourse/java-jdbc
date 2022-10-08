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
    private static final int FIRST_INDEX = 0;
    private static final int PARAMETER_INDEX_INCREMENT = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        try (final Connection connection = getConnection();
             final PreparedStatement preparedStatement = createPreparedStatement(connection, sql, args)) {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("error : {}", e);
            throw new RuntimeException("해당 sql문을 실행할 수 없습니다.", e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = getConnection();
             final PreparedStatement preparedStatement = createPreparedStatement(connection, sql, args);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        } catch (SQLException e) {
            log.error("error : {}", e);
            throw new RuntimeException("해당 sql문을 실행할 수 없습니다.", e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> query = query(sql, rowMapper, args);
        return query.get(FIRST_INDEX);
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private PreparedStatement createPreparedStatement(final Connection connection, final String sql,
                                                      final Object... args) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = FIRST_INDEX; i < args.length; i++) {
            preparedStatement.setObject(i + PARAMETER_INDEX_INCREMENT, args[i]);
        }
        return preparedStatement;
    }
}
