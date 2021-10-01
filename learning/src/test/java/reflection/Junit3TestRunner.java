package reflection;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class Junit3TestRunner {

    @Test
    void run() throws Exception {
        Class<Junit3Test> clazz = Junit3Test.class;

        Method[] declaredMethods = clazz.getDeclaredMethods();
        Junit3Test instance = clazz.getDeclaredConstructor().newInstance();
        for (Method method : declaredMethods) {
            if (method.getName().startsWith("test")) {
                method.invoke(instance);
            }
        }
    }
}
