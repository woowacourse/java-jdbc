package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private static final ParameterSetter DEFAULT_SETTER = (index, ps, param) -> ps.setObject(index, param);
    private static final Map<Class<?>, ParameterSetter> parameterSetters = Map.of(
            Integer.class, (index, ps, param) -> ps.setInt(index, (Integer) param),
            Long.class, (index, ps, param) -> ps.setLong(index, (Long) param),
            Double.class, (index, ps, param) -> ps.setDouble(index, (Double) param),
            String.class, (index, ps, param) -> ps.setString(index, (String) param),
            Boolean.class, (index, ps, param) -> ps.setBoolean(index, (Boolean) param),
            LocalDateTime.class, (index, ps, param) -> ps.setTimestamp(index, Timestamp.valueOf((LocalDateTime) param)),
            Object.class, (index, ps, param) -> ps.setObject(index, param));

    private final Object[] args;

    public ArgumentPreparedStatementSetter(Object... args) {
        this.args = args;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            Object param = args[i];
            ParameterSetter setter = getParameterSetter(param);
            setter.setParameter(i + 1, ps, param);
        }
    }

    private ParameterSetter getParameterSetter(Object parameter) {
        System.out.println(parameter.getClass());
        ParameterSetter setter = parameterSetters.get(parameter.getClass());
        if(setter == null) {
            setter = DEFAULT_SETTER;
        }
        return setter;
    }
}
