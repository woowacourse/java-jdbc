package com.interface21.jdbc.result;

import java.util.List;
import java.util.stream.Stream;

public record MultiSelectResult(List<SingleSelectResult> results) {
    public Stream<SingleSelectResult> stream() {
        return results.stream();
    }
}
