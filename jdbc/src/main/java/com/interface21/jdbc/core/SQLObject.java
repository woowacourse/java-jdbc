package com.interface21.jdbc.core;

import java.sql.SQLType;

public record SQLObject(int parameterIndex, Object value, SQLType type) {
}
