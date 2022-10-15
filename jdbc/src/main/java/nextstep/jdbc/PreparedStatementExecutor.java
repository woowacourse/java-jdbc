package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
interface PreparedStatementExecutor<T> {

    static PreparedStatementExecutor<Integer> executeUpdateWithKeyHolder(final KeyHolder<?> keyHolder) {
        return pstmt -> {
            final int result = pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                while (keys.next()) {
                    keyHolder.addKey(keys.getObject(1));
                }
            }
            return result;
        };
    }

    static <T> PreparedStatementExecutor<T> mapToObjectExecutor(final RowMapper<T> rowMapper) {
        return pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rowMapper.map(rs) : null;
            }
        };
    }

    static <T> PreparedStatementExecutor<List<T>> mapToListExecutor(final RowMapper<T> rowMapper) {
        return pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return mapToList(rowMapper, rs);
            }
        };
    }

    private static <T> List<T> mapToList(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.map(rs));
        }
        return result;
    }

    T execute(PreparedStatement pstmt) throws SQLException;
}
