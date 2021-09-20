package nextstep.jdbc;

import java.sql.*;
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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            PreparedStatementSetter setter = argumentPreparedStatementSetter(args);
            setter.setValues(pstmt);

            log.debug("query : {}", sql);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper) {
        RowMapperResultSetExtractor<T> extractor = new RowMapperResultSetExtractor<>(mapper);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery(sql)) {
            return extractor.extractData(resultSet);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = executeQuery(argumentPreparedStatementSetter(args), pstmt)) {

            log.debug("query : {}", sql);

            if (rs.next()) {
                return mapper.mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    private ResultSet executeQuery(PreparedStatementSetter setter, PreparedStatement pstmt) throws SQLException {
        setter.setValues(pstmt);
        return pstmt.executeQuery();
    }

    private PreparedStatementSetter argumentPreparedStatementSetter(Object[] args) {
        return new ArgumentPreparedStatementSetter(args);
    }
}
