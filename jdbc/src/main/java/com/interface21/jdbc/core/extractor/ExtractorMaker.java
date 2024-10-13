package com.interface21.jdbc.core.extractor;

import java.sql.ResultSet;

@FunctionalInterface
public interface ExtractorMaker<T> {
      ResultSetExtractor<T> from(ResultSet resultSet);
}
