package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TypeBasedPreparedStatementSetter implements PreparedStatementSetter {

    private final List<SQLObject> sqlObjects;

    public TypeBasedPreparedStatementSetter(SQLObject... sqlObjects) {
        this.sqlObjects = List.of(sqlObjects);
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        for (SQLObject sqlObject : sqlObjects) {
            ps.setObject(sqlObject.parameterIndex(), sqlObject.value(), sqlObject.type());
        }
    }
}
