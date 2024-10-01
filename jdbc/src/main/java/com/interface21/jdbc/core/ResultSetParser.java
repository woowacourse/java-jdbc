package com.interface21.jdbc.core;

import java.sql.ResultSet;

public interface ResultSetParser {
    <T> T parse(ResultSet resultSet);
}
