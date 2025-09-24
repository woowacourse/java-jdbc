package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter {

    void execute(final PreparedStatement pmtst, final Integer index, final Object value) throws SQLException;
}
