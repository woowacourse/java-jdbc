package com.interface21.jdbc.support.h2;

import java.util.List;
import com.interface21.jdbc.support.ErrorCodes;

class H2CanNotAcquireLockCodes implements ErrorCodes {

    private final List<Integer> codes;

    public H2CanNotAcquireLockCodes() {
        this.codes = List.of(50200);
    }

    @Override
    public boolean contains(int code) {
        return false;
    }
}
