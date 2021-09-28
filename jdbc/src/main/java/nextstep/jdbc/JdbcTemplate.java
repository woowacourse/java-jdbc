package nextstep.jdbc;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String query, Object... params) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            setValues(pstmt, params);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    public <T> T queryForObject(String query, RowMapper<T> rowMapper, Object... params) {
        List<T> queryResult = query(query, rowMapper, params);
        if (queryResult.size() > 1) {
            throw new DataException("duplication result");
        }
        return queryResult.get(0);
    }

    public <T> List<T> query(String query, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            setValues(pstmt, params);
            pstmt.executeQuery();
            return mapping(pstmt, rowMapper);
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    private <T> List<T> mapping(PreparedStatement pstmt, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet resultSet = pstmt.executeQuery()) {
            return doMapping(pstmt.executeQuery(), rowMapper);
        }
    }

    private <T> List<T> doMapping(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> data = new ArrayList<>();
        while (resultSet.next()) {
            data.add(rowMapper.mapRow(resultSet));
        }
        if (data.isEmpty()) {
            throw new SQLException();
        }
        return data;
    }

    private void setValues(PreparedStatement pstmt, Object... params) throws SQLException {
        int parameterNumber = 1;
        for (Object param : params) {
            pstmt.setObject(parameterNumber++, param);
        }
    }
}
