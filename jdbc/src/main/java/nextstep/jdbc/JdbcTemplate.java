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

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        execute(sql, pstmt -> setValues(pstmt, args), PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... args) {
        RowMapperResultSetExtractor<T> extractor = new RowMapperResultSetExtractor<>(mapper);
        return execute(sql,
                pstmt -> setValues(pstmt, args),
                pstmt -> {
                    try (ResultSet resultSet = pstmt.executeQuery()) {
                        return extractor.extractData(resultSet);
                    }
                });
    }

    private void setValues(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            Object value = args[i];
            pstmt.setObject(i + 1, value);
        }
    }

    private <T> T execute(String sql, PreparedStatementSetter setter, PreparedStatementCallback<T> callback) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setter.setValues(pstmt);
            log.debug("query : {}", sql);
            return callback.execute(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... args) {
        return query(sql, mapper, args)
                .stream()
                .findFirst()
                .orElseThrow(() -> new DataAccessException("존재하지 않는 데이터입니다."));
    }
}
