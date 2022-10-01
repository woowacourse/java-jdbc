package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setArguments(pstmt, args);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.debug("ERROR CODE: {} SQL STATE: {}", e.getErrorCode(), e.getSQLState());
            throw new DataAccessException(e);
        }
    }

    private void setArguments(PreparedStatement preparedStatement, Object[] args) throws SQLException {
        int parameterIndex = 0;
        for (Object arg : args) {
            preparedStatement.setObject(parameterIndex++, arg);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
