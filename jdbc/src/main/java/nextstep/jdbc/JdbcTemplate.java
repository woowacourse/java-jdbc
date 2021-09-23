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

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException();
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("no result!");
            }
            return rowMapper.mapRow(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException();
        }
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            ResultSet rs = pstmt.executeQuery();
            List<T> targets = new ArrayList<>();

            while (rs.next()) {
                targets.add(rowMapper.mapRow(rs));
            }
            return targets;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException();
        }
    }
}
