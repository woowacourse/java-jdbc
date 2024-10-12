package com.interface21.jdbc.support.h2;

import java.util.List;
import com.interface21.jdbc.support.ErrorCodes;

class H2BadGrammarsErrorCodes implements ErrorCodes {

    private final List<Integer> codes;

    public H2BadGrammarsErrorCodes() {
        this.codes = List.of(42000, 42001, 42101, 42102, 42111, 42112, 42121, 42122, 42132);
    }

    @Override
    public boolean contains(int code) {
        return codes.contains(code);
    }
}
