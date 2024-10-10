package com.interface21.jdbc.core;

import java.sql.SQLType;

public record SQLParameter(int parameterIndex, Object value, SQLType type) {
}
