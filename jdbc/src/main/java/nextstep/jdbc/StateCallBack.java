package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public interface StateCallBack<T> {

    T doInStatement(Statement stmt, ResultSet rs) throws SQLException;
}
