package com.interface21.jdbc.dsl.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Values {

    private final List<DbObject> valueList = new ArrayList<>();

    public void add(Object... value) {
        Arrays.stream(value).map(DbObject::new).forEach(valueList::add);
    }

    @Override
    public String toString() {
        String values = valueList.stream().map(DbObject::toString).reduce((a, b) -> a + ", " + b).orElse("");
        return "VALUES (%s)".formatted(values);
    }
}
