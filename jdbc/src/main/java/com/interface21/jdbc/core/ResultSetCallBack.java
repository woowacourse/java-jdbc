package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetCallBack<T> {

    T callback(ResultSet resultSet) throws SQLException;
}
