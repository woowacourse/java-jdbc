package nextstep.web;

import java.util.Map;

public interface BeanScanner {

    Map<Class<?>, Object> scan();
}
