package nextstep.jdbc;

import java.util.ArrayList;
import java.util.List;

public class Parameters {

    private final List<Object> parameters;

    public Parameters() {
        this.parameters = new ArrayList<>();
    }

    public void addParam(final Object param) {
        parameters.add(param);
    }

    public List<Object> getParameters() {
        return parameters;
    }
}
