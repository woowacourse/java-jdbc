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

public abstract class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public abstract DataSource getDataSource();

    public final void update(String sql, Object... objects) {

        try (Connection conn = getDataSource().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setPstmt(pstmt, objects);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException("Cannot update");
        }
    }

    public final <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... objects) {
        try (Connection conn = getDataSource().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = executeQuery(pstmt, objects)) {
            log.debug("query : {}", sql);

            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }

            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException("Cannot query for object");
        }
    }

    public final <T> List<T> query(String sql, RowMapper<T> rowMapper) {

        try (Connection conn = getDataSource().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);

            List<T> entities = new ArrayList<>();

            while (rs.next()) {
                entities.add(rowMapper.mapRow(rs));
            }

            return entities;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException("Cannot query");
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt, Object[] objects) throws SQLException {
        setPstmt(pstmt, objects);
        return pstmt.executeQuery();
    }

    private void setPstmt(PreparedStatement pstmt, Object[] objects) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            PreparedStatementSetter pstmts = SqlParameterValue.findSetter(object.getClass());
            pstmts.setValue(pstmt, i + 1, object);
        }
    }
}
