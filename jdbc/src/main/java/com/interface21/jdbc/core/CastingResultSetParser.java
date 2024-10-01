package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract non-sealed class CastingResultSetParser<T> implements ResultSetParser<T> {

    private static final Logger log = LoggerFactory.getLogger(CastingResultSetParser.class);

    private final Class<T> castingType;

    public CastingResultSetParser(Class<T> castingType) {
        this.castingType = castingType;
    }

    @Override
    public final T parse(ResultSet resultSet) {
        try {
            return extractData(resultSet);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private T extractData(ResultSet resultSet) throws SQLException {
        Object rawData = parseInternal(resultSet);
        if (castingType.isInstance(rawData)) {
            return castingType.cast(rawData);
        }
        throw new RuntimeException("반환값의 타입을 변환할 수 없습니다!");
    }

    protected abstract Object parseInternal(ResultSet resultSet) throws SQLException;
}
