package com.interface21.jdbc.core.querybuilder.select;

public interface SelectColumnsStep<T> {
    SelectFromStep<T> columns(String... columns);
    // 모든 컬럼 (*) 지정 (주의: 일반적으로 권장되지 않음)
    SelectFromStep<T> allColumns();
}
