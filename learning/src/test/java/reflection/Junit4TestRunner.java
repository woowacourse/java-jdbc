package reflection;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class Junit4TestRunner {

    @Test
    void run() throws Exception {
        Class<Junit4Test> clazz = Junit4Test.class;
        Junit4Test junit4Test = clazz.getConstructor().newInstance();

        List<Method> methods = getMyTestMethods(clazz);
        for (Method method : methods) {
            method.invoke(junit4Test);
        }

    }

    private List<Method> getMyTestMethods(Class<Junit4Test> clazz) {
        return Arrays.stream(clazz.getMethods())
            .filter(method -> method.isAnnotationPresent(MyTest.class))
            .collect(Collectors.toList());
    }
}
