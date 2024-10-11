package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StatementParamSetter {

    private static final Map<Class<?>, Setter> SET_MAPPER = Map.of(
            String.class, (ps, index, param) -> ps.setString(index.getAndIncrement(), (String) param),
            Integer.class, (ps, index, param) -> ps.setInt(index.getAndIncrement(), (Integer) param),
            Long.class, (ps, index, param) -> ps.setLong(index.getAndIncrement(), (Long) param),
            Boolean.class, (ps, index, param) -> ps.setBoolean(index.getAndIncrement(), (Boolean) param)
    );

    private interface Setter {
        void accept(PreparedStatement preparedStatement, AtomicInteger index, Object param) throws SQLException;
    }

    public static void setParams(PreparedStatement ps, Object... params) {
        AtomicInteger index = new AtomicInteger(1);
        Arrays.stream(params)
                .forEach(ConsumerWrapper.accept(param -> setParams(ps, param, index)));
    }

    private static void setParams(PreparedStatement ps, Object param, AtomicInteger index) throws SQLException {
        Setter setter = SET_MAPPER.get(param.getClass());
        setter.accept(ps, index, param);
    }
}
