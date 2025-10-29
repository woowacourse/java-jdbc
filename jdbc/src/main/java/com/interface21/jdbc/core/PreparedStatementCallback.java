package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

//https://github.com/spring-projects/spring-framework/blob/6aeb9d16e83c69d980f48a4a4f052bf52f31dfd0/spring-jdbc/src/main/java/org/springframework/jdbc/core/PreparedStatementCallback.java#L47
@FunctionalInterface
public interface PreparedStatementCallback<T> {

    T doInPreparedStatement(PreparedStatement ps) throws SQLException;
}
