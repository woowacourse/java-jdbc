package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ThrowingBiFunction<T, R> {

    R apply(final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException;
}
