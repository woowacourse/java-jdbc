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
    private static final List<PreparedStatementSetter> pstmtSetters = new ArrayList<>();

    static {
        pstmtSetters.add(new StringSetter());
        pstmtSetters.add(new LongSetter());
    }

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setPreparedStatement(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void insert(String sql, Object... args){
        update(sql, args);
    }

    public void delete(String sql, Object... args) {
        update(sql, args);
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = createPreparedStatement(conn, sql, args);
                ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);

            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            log.debug("query : {}", sql);

            List<T> users = new ArrayList<>();
            while (rs.next()) {
                T t = rowMapper.mapRow(rs);
                users.add(t);
            }
            return users;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setPreparedStatement(PreparedStatement pstmt, Object[] args) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            for (PreparedStatementSetter setter : pstmtSetters) {
                setter.set(pstmt, index, arg);
            }
            index += 1;
        }
    }

    private PreparedStatement createPreparedStatement(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        setPreparedStatement(pstmt, args);
        return pstmt;
    }
}
