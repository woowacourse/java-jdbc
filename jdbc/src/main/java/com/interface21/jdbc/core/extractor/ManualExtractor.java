package com.interface21.jdbc.core.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nullable;

public final class ManualExtractor<T> extends ResultSetExtractor<T> {
    private final ExtractionRule<T> extractionRule;

    public ManualExtractor(ResultSet resultSet, ExtractionRule<T> extractionRule) {
        super(resultSet);
        this.extractionRule = extractionRule;
    }

    @Nullable
    @Override
    public T extractOne() throws SQLException {
        return extractionRule.apply(this.resultSet);
    }
}
