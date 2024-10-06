package com.interface21.jdbc.result;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SelectMultiResult(Map<String, List<Object>> columnMap, int size) {

    @SuppressWarnings("unchecked")
    public <T> T getColumnList(final String columnName) {
        try {
            return (T) Optional.ofNullable(columnMap.get(columnName.toUpperCase()))
                    .orElseThrow(() -> new IllegalArgumentException(String.format("%s 에 대한 값은 존재하지 않습니다.", columnName)));
        } catch (final ClassCastException e) {
            throw new IllegalArgumentException(String.format("타입 변환에 실패했습니다. 잘못된 값 : %s ", columnName), e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getColumnValue(final String columnName, final int count) {
        try {
            return (T) Optional.ofNullable(columnMap.get(columnName.toUpperCase()).get(count))
                    .orElseThrow(() -> new IllegalArgumentException(String.format("%s 에 대한 값은 존재하지 않습니다.", columnName)));
        } catch (final ClassCastException e) {
            throw new IllegalArgumentException(String.format("타입 변환에 실패했습니다. 잘못된 값 : %s ", columnName), e);
        }
    }
}
