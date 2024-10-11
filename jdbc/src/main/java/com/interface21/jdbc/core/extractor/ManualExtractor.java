package com.interface21.jdbc.core.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class ManualExtractor<T> extends ResultSetExtractor<T> {
    private final Function<ResultSet, T> function;

    public ManualExtractor(ResultSet resultSet, Function<ResultSet, T> go) {
        super(resultSet);
        this.function = go;
    }

    @Nonnull
    @Override
    public T extractOne() throws SQLException {
        return this.function.apply(this.resultSet);
    }
}
