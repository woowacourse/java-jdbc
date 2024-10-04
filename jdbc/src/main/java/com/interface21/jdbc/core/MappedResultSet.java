package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class MappedResultSet<T> {

    private static final int NO_LIMIT = -1;

    private final List<T> results;

    public static <T> MappedResultSet<T> create(RowMapper<T> rowMapper, PreparedStatement preparedStatement)
            throws SQLException {
        return create(rowMapper, preparedStatement, NO_LIMIT);
    }

    public static <T> MappedResultSet<T> create(RowMapper<T> rowMapper, PreparedStatement preparedStatement, int limitCount)
            throws SQLException {
        List<T> results = new ArrayList<>();
        AtomicInteger remainingCount = new AtomicInteger(limitCount);
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (next(resultSet, remainingCount)) {
                results.add(rowMapper.map(resultSet));
            }
        }

        return new MappedResultSet<>(results);
    }

    private static boolean next(ResultSet resultSet, AtomicInteger remainingCount) throws SQLException {
        if (remainingCount.get() == 0 || !resultSet.next()) {
            return false;
        }
        if (remainingCount.get() != NO_LIMIT) {
            remainingCount.decrementAndGet();
        }
        return true;
    }

    public MappedResultSet(List<T> results) {
        this.results = new ArrayList<>(results);
    }

    public List<T> getResults() {
        return Collections.unmodifiableList(results);
    }

    public Optional<T> getFirst() {
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.getFirst());
    }
}
