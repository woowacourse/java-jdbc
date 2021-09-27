package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface Query<T> {
    
    T doQuery(PreparedStatement preparedStatement) throws SQLException;
}