package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
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

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            FindFunction<T> findFunction = this::find;
            return mapList(findFunction.execute(stmt, sql), rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            FindFunction<T> findFunction = this::find;
            return rowMapper.mapRow(findFunction.execute(stmt, sql));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private ResultSet find(PreparedStatement smtp, String sql) throws SQLException {
        final ResultSet rs = smtp.executeQuery();
        log.debug("query : {}", sql);
        if (rs.next()) {
            log.debug("result found!");
            return rs;
        }
        log.debug("result not found!");
        return null;
    }

    public void executeUpdate(String sql, StmtSettingFunction stmtSettingFunction) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            PreparedStatement statement = stmtSettingFunction.execute(stmt, sql);
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> mapList(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> result = new LinkedList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs));
        }
        return result;
    }
}
