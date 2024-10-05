package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Maker {

    public Object make(ResultSet resultSet) throws SQLException;
}
