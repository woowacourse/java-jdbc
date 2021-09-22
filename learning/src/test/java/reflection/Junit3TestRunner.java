package reflection;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class Junit3TestRunner {

    @Test
    void run() throws Exception {
        Class<Junit3Test> clazz = Junit3Test.class;
        Junit3Test junit3Test = clazz.getConstructor().newInstance();

        List<Method> methods = getTestMethods(clazz);
        for (Method method : methods) {
            method.invoke(junit3Test);
        }
    }

    private List<Method> getTestMethods(Class<Junit3Test> clazz) {
        return Arrays.stream(clazz.getMethods())
            .filter(method -> method.getName().startsWith("test"))
            .collect(Collectors.toList());
    }
}
