package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter {

    void set(final PreparedStatement pmtst, final Integer index, final Object value) throws SQLException;
}
