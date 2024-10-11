package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class StatementParamSetter {

    public static void setParams(PreparedStatement ps, Object... params) {
        AtomicInteger index = new AtomicInteger(1);
        Arrays.stream(params)
                .forEach(ConsumerWrapper.accept(param -> ps.setObject(index.getAndIncrement(), param)));
    }
}
