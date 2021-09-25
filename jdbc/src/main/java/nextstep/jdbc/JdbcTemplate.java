package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.exception.DataAccessException;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object ... parameters) {
        execute(sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object ... parameters) {
        return execute(sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
                return null;
            } catch (Exception e) {
                throw new DataAccessException(e.getMessage());
            }
        }, parameters);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return execute(sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(rowMapper.mapRow(rs));
                }
                return list;
            } catch (SQLException e) {
                throw new DataAccessException(e.getMessage());
            }
        });
    }

    private <T> T execute(String sql, PreparedStatementSetter<T> preparedStatementSetter, Object ... parameters) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int index = 1;
            for (Object parameter : parameters) {
                pstmt.setObject(index, parameter);
                index++;
            }
            return preparedStatementSetter.execute(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
