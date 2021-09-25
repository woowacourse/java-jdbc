package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(String sql, Object... args) {
        class CreateTemplate implements ActionTemplate {
            @Override
            public <T> T action(PreparedStatement pst, String sql, Object[] args) throws SQLException {
                setValues(pst, args);
                pst.executeUpdate();
                return null;
            }
        }
        makeResult(new CreateTemplate(), sql, args);
    }

    public void update(String sql, Object... args) {
        class UpdateTemplate implements ActionTemplate {
            @Override
            public <T> T action(PreparedStatement pst, String sql, Object[] args) throws SQLException {
                setValues(pst, args);
                pst.executeUpdate();
                return null;
            }
        }
        makeResult(new UpdateTemplate(), sql, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        class FindAllTemplate implements ActionTemplate {
            @Override
            public List<T> action(PreparedStatement pst, String sql, Object[] args) throws SQLException {
                try (ResultSet rs = pst.executeQuery()) {
                    List<T> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(rowMapper.mapRow(rs));
                    }
                    return result;
                }
            }
        }
        return makeResult(new FindAllTemplate(), sql, null);
    }

    public <T> T queryObject(String sql, RowMapper<T> rowMapper, Object... args) {
        class FindTemplate implements ActionTemplate {
            @Override
            public T action(PreparedStatement pst, String sql, Object[] args) throws SQLException {
                setValues(pst, args);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rowMapper.mapRow(rs);
                    }
                    return null;
                }
            }
        }
        return makeResult(new FindTemplate(), sql, args);
    }

    private <T> T makeResult(ActionTemplate actionTemplate, String sql, Object[] args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            return actionTemplate.action(pst, sql, args);
        } catch (SQLException e) {
            throw new JdbcCustomException(e.getMessage());
        }
    }

    private void setValues(PreparedStatement pst, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pst.setObject(i + 1, args[i]);
        }
    }
}
