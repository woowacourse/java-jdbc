package reflection;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class Junit3TestRunner {

    @Test
    void run() throws Exception {
        Class<Junit3Test> clazz = Junit3Test.class;

        Junit3Test junit3Test = clazz.getConstructor().newInstance();
        Arrays.stream(clazz.getMethods())
            .filter(method -> method.getName().startsWith("test"))
            .forEach(testMethod -> {
                try {
                    testMethod.invoke(junit3Test);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
    }
}
