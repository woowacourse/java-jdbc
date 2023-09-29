package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class PreparedStatementSetter {

    private final List<PreparedStatementTypeValue> values;

    public PreparedStatementSetter(final List<Object> values) {
        this.values = values.stream()
                .map(PreparedStatementTypeValue::new)
                .collect(Collectors.toList());
    }

    public void set(final PreparedStatement ps) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            final PreparedStatementTypeValue pstv = values.get(i);
            pstv.setPreparedStatement(ps, i+1);
        }
    }
}
