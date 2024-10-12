package com.interface21.jdbc.support.h2;

import java.util.List;
import com.interface21.jdbc.support.ErrorCodes;

class H2DataAccessResourceFailureCodes implements ErrorCodes {

    private final List<Integer> codes;

    public H2DataAccessResourceFailureCodes() {
        this.codes = List.of(90046, 90100, 90117, 90121, 90126);
    }

    @Override
    public boolean contains(int code) {
        return false;
    }
}
