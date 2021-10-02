package nextstep.jdbc.resultset.strategy.creation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetCreationStrategy {
    ResultSet create(PreparedStatement pstmt) throws SQLException;
}
