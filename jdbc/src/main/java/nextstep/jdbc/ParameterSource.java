package nextstep.jdbc;

import java.util.ArrayList;
import java.util.List;

public class ParameterSource {

    private final List<Object> params = new ArrayList<>();

    public void addParam(Object value) {
        params.add(value);
    }

    public int getParamCount() {
        return params.size();
    }

    public Object getParam(int index) {
        return params.get(index);
    }
}
