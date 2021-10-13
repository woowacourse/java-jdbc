package reflection;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

class Junit4TestRunner {

    @Test
    void run() throws Exception {
        Class<Junit4Test> clazz = Junit4Test.class;

        Junit4Test junit4Test = clazz.getConstructor().newInstance();

        Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(MyTest.class))
                .forEach(method -> {
                    try {
                        method.invoke(junit4Test);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
    }
}
