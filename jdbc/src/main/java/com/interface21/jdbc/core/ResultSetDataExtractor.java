package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface ResultSetDataExtractor<T> {

    List<T> extractData(PreparedStatement preparedStatement) throws SQLException;
}
