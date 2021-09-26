package reflection;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class Junit4TestRunner {

    @Test
    void run() throws Exception {
        Class<Junit4Test> clazz = Junit4Test.class;

        Junit4Test junit4Test = clazz.getConstructor().newInstance();
        Arrays.stream(clazz.getMethods())
            .filter(method -> method.isAnnotationPresent(MyTest.class))
            .forEach(myTestMethod -> {
                try {
                    myTestMethod.invoke(junit4Test);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
    }
}
