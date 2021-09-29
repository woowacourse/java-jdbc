package nextstep.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebApplicationContext {

    private final Map<Class<?>, Object> beans = new HashMap<>();
    private final List<BeanScanner> beanScanners = new ArrayList<>();

    public void addBeanScanner(final BeanScanner beanScanner) {
        beanScanners.add(beanScanner);
    }

    public void initialize() {
        for (BeanScanner beanScanner : beanScanners) {
            Map<Class<?>, Object> scanBeans = beanScanner.scan();

            scanBeans.forEach(beans::putIfAbsent);
        }
    }

    public <T> T getBean(final Class<T> clazz) {
        if (!beans.containsKey(clazz)) {
            throw new IllegalArgumentException(String.format("존재하지 않는 Bean입니다.(%s)", clazz.toString()));
        }
        Object bean = beans.get(clazz);
        return clazz.cast(bean);
    }
}
