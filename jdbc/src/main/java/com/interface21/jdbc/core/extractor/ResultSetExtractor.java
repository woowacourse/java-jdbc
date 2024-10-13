package com.interface21.jdbc.core.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class ResultSetExtractor<T> implements AutoCloseable {
    protected final ResultSet resultSet;

    public ResultSetExtractor(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Nonnull
    public final List<T> extract() throws SQLException {
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(extractOne());
        }
        return result;
    }

    @Nullable
    public T extractOne() throws SQLException {
        throw new RuntimeException();
    }

    @Override
    public void close() throws SQLException {
        resultSet.close();
    }
}
