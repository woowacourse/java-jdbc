package com.interface21.jdbc.result;

import java.util.List;
import java.util.stream.Stream;

public record SelectMultiResult(List<SelectSingleResult> results) {
    public Stream<SelectSingleResult> stream() {
        return results.stream();
    }
}
