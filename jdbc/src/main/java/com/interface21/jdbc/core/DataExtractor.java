package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface DataExtractor {

    <T> List<T> extract(final ResultSet resultSet, final RowMapper<T> rowMapper)
            throws SQLException;
}
