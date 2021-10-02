package reflection;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

class Junit3TestRunner {

    @Test
    void run() throws Exception {
        Class<Junit3Test> clazz = Junit3Test.class;

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                method.invoke(clazz.getConstructor().newInstance());
            }
        }
    }
}
