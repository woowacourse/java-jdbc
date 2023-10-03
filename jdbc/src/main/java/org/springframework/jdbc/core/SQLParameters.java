package org.springframework.jdbc.core;

import java.util.ArrayList;
import java.util.List;

public class SQLParameters {
    private final List<Object> parameters = new ArrayList<>();

    public SQLParameters addParameter(final Object parameter) {
        parameters.add(parameter);
        return this;
    }

    public List<Object> getParameters() {
        return parameters;
    }
}
