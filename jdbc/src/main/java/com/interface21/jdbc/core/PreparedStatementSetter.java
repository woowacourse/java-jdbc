package com.interface21.jdbc.core;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface PreparedStatementSetter {

    void setValues(PreparedStatement ps);
}
