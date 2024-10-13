package com.interface21.jdbc.core.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ExtractionRule<T> {
    T apply(ResultSet resultSet) throws SQLException;
}
