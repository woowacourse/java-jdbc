package com.interface21.jdbc.core.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class ResultSetExtractor<T> implements AutoCloseable {
    protected final ResultSet resultSet;

    public ResultSetExtractor(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Nonnull
    public List<T> extract() throws SQLException {
        if (resultSet.isAfterLast()) {
            return List.of();
        }

        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(extractOne());
        }

        return result;
    }

    public T extractOne() throws SQLException {
        throw new RuntimeException();
    }

    @Override
    public void close() throws SQLException {
        resultSet.close();
    }
}
