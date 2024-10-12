package com.interface21.jdbc.support.h2;

import java.util.List;
import com.interface21.dao.BadGrammarException;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.support.AbstractErrorCodes;

class H2BadGrammarsErrorCodes extends AbstractErrorCodes {

    public H2BadGrammarsErrorCodes() {
        super(List.of(42000, 42001, 42101, 42102, 42111, 42112, 42121, 42122, 42132));
    }

    @Override
    public Class<? extends DataAccessException> translateTargetClass() {
        return BadGrammarException.class;
    }
}
