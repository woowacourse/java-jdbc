package com.interface21.jdbc.dsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColumnNames {

    private final List<String> nameList = new ArrayList<>();

    public void add(String... name) {
        nameList.addAll(Arrays.asList(name));
    }

    @Override
    public String toString() {
        return nameList.stream().reduce((a, b) -> a + ", " + b).orElse("");
    }
}
