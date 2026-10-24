package com.techcourse.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter {

    void setValues(PreparedStatement statement) throws SQLException;
}
