package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(String sql, Class<T> clazz, Object... args) {
        return execute(sql, (preparedStatement -> getResult(clazz, preparedStatement)), args);
    }

    public <T> T queryForObject(String sql, Class<T> clazz, Object... args) {
        final List<T> result = query(sql, clazz, args);
        return result.get(0);
    }

    private <T> List<T> getResult(Class<T> clazz, PreparedStatement pstmt) {
        try (final ResultSet resultSet = pstmt.executeQuery()) {
            return ResultDataExtractor.extractData(resultSet, clazz);
        } catch (SQLException exception) {
            throw new DataAccessException();
        }
    }

    public void update(String sql, Object... args) {
        execute(sql, PreparedStatement::executeUpdate, args);
    }

    private <T> T execute(String sql, PreparedStatementSetter<T> pstmtSetter, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            setParameters(pstmt, args);
            return pstmtSetter.execute(pstmt);
        } catch (Exception exception) {
            throw new DataAccessException();
        }
    }

    private void setParameters(PreparedStatement pstmt, Object[] objects) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            pstmt.setObject(i+1, objects[i]);
        }
    }
}
