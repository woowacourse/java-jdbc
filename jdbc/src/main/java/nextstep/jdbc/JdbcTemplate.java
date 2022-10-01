package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            setStatement(statement, args);
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("sql 실행 중 에러가 발생하였습니다.");
        }
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, Object... args) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            setStatement(statement, args);
            ResultSet rs = statement.executeQuery();

            log.debug("query : {}", sql);

            return ResultSetExtractor.extract(rowMapper, rs);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("sql 실행 중 에러가 발생하였습니다.");
        }
    }

    public <T> List<T> query(final String sql, RowMapper<T> rowMapper) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
        ) {
            return ResultSetExtractor.extractList(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("sql 실행 중 에러가 발생하였습니다.");
        }
    }

    private void setStatement(final PreparedStatement statement, final Object[] data) throws SQLException {
        for (int i = 0; i < data.length; i++) {
            statement.setObject(i + 1, data[i]);
        }
    }
}
