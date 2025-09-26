package com.interface21.jdbc.dsl;

import java.util.ArrayList;
import java.util.List;

public class Values {

    private final List<DbObject> valueList = new ArrayList<>();

    void add(Object value) {
        valueList.add(new DbObject(value));
    }

    @Override
    public String toString() {
        String values = valueList.stream().map(DbObject::toString).reduce((a, b) -> a + ", " + b).orElse("");
        return "VALUES (%s)".formatted(values);
    }
}
