package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public sealed interface ResultSetParser<T> permits CastingResultSetParser {
    T parse(ResultSet resultSet);
}
