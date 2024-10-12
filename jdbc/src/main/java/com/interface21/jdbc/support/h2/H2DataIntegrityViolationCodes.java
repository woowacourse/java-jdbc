package com.interface21.jdbc.support.h2;

import java.util.List;
import com.interface21.jdbc.support.ErrorCodes;

class H2DataIntegrityViolationCodes implements ErrorCodes {

    private final List<Integer> codes;


    public H2DataIntegrityViolationCodes() {
        this.codes = List.of(22001, 22003, 22012, 22018, 22025, 23000, 23002, 23003, 23502, 23503, 23506, 23507, 23513);
    }

    @Override
    public boolean contains(int code) {
        return false;
    }
}
