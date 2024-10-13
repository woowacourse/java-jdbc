package com.interface21.jdbc.core.extractor;

import com.interface21.jdbc.CannotReleaseJdbcResourceException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    protected abstract T extractOne() throws SQLException;

    @Override
    public void close() {
        try {
            resultSet.close();
        } catch (SQLException e) {
            throw new CannotReleaseJdbcResourceException(e);
        }
    }
}
