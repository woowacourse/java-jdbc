package org.springframework.jdbc.core;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface PreparedStatementSetter {
    void setValues(PreparedStatement pstmt);
}
