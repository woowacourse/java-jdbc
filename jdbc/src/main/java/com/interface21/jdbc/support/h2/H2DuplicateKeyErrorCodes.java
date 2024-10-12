package com.interface21.jdbc.support.h2;

import java.util.List;
import com.interface21.jdbc.support.ErrorCodes;

class H2DuplicateKeyErrorCodes implements ErrorCodes {

    private final List<Integer> codes;

    public H2DuplicateKeyErrorCodes() {
        this.codes = List.of(23001, 23505);
    }

    @Override
    public boolean contains(int code) {
        return false;
    }
}
