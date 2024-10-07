package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Parameters {

    private final Map<Integer, Object> parameters;

    public Parameters() {
        this.parameters = new HashMap<>();
    }

    public void add(int index, Object value) {
        parameters.put(index, value);
    }

    public void setPreparedStatement(PreparedStatement pstmt) throws SQLException {
        for (Map.Entry<Integer, Object> entry : parameters.entrySet()) {
            pstmt.setObject(entry.getKey(), entry.getValue());
        }
    }
}
