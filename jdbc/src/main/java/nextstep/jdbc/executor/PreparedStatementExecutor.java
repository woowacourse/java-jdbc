package nextstep.jdbc.executor;

import nextstep.jdbc.RowMapper;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PreparedStatementExecutor {

    private PreparedStatementExecutor() {
    }

    public static int update(PreparedStatement pstmt, Object[] params) throws SQLException {
        setArguments(pstmt, params);
        return pstmt.executeUpdate();
    }

    public static <T> List<T> query(PreparedStatement pstmt, Object[] params, RowMapper<T> mapper) throws SQLException {
        setArguments(pstmt, params);
        try (ResultSet resultSet = pstmt.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(mapper.mapRow(resultSet));
            }
            return results;
        }
    }

    public static <T> T queryForObject(PreparedStatement pstmt, Object[] params, RowMapper<T> mapper) throws SQLException {
        List<T> results = query(pstmt, params, mapper);
        if (results.size() != 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.get(0);
    }

    private static void setArguments(PreparedStatement pstmt, Object[] params) throws SQLException {
        for (int index = 0; index < params.length; index++) {
            pstmt.setObject(index + 1, params[index]);
        }
    }
}
