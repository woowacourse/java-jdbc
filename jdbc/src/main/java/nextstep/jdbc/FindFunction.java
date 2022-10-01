package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface FindFunction<T> {
     ResultSet execute(PreparedStatement stmt, String sql) throws SQLException;
}
