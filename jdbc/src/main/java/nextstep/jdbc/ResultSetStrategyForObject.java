package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetStrategyForObject {

    void setParameters(PreparedStatement pstmt) throws SQLException;

    Object mapRows(ResultSet resultSet) throws SQLException;
}
