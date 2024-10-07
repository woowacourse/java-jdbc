package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ObjectMaker<T> {

    T make(ResultSet resultSet) throws SQLException;
}
