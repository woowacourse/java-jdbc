package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetExtractor {

    public static <T> T toObject(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        ResultSet rs = pstmt.executeQuery();

        if (!rs.next()) {
            return null;
        }
        return rowMapper.mapRow(rs);
    }

    public static <T> List<T> toList(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        ResultSet rs = pstmt.executeQuery();

        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs));
        }
        return result;
    }
}
