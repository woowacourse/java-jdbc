package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.util.stream.IntStream;

public class PreparedStatementSetter {
    public static PreparedStatement setArguments(PreparedStatement preparedStatement, Object[] args) {
        IntStream.range(0, args.length)
                .forEach(index -> ParameterSetter.apply(preparedStatement, index + 1, args[index]));
        return preparedStatement;
    }
}
