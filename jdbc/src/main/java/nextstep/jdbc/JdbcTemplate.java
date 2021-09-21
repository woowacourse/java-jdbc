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
        PreparedStatementSetter setter = argumentPreparedStatementSetter(args);
        execute(sql, pstmt -> {
            setter.setValues(pstmt);
            return pstmt.executeUpdate();
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... args) {
        RowMapperResultSetExtractor<T> extractor = new RowMapperResultSetExtractor<>(mapper);
        PreparedStatementSetter setter = argumentPreparedStatementSetter(args);
        return execute(sql, pstmt -> {
            ResultSet resultSet = null;
            try {
                setter.setValues(pstmt);
                resultSet = pstmt.executeQuery();
                return extractor.extractData(resultSet);
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        });
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

    private PreparedStatementSetter argumentPreparedStatementSetter(Object[] args) {
        return new ArgumentPreparedStatementSetter(args);
    }
}
