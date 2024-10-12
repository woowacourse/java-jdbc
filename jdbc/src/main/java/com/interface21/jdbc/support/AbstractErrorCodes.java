package com.interface21.jdbc.support;

import java.util.List;

public abstract class AbstractErrorCodes implements ErrorCodes {

    private final List<Integer> codes;

    public AbstractErrorCodes(List<Integer> codes) {
        this.codes = codes;
    }

    @Override
    public boolean contains(int code) {
        return codes.contains(code);
    }
}
