package reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

class Junit3TestRunner {

    @DisplayName("Junit3Test에서 test로 시작하는 메소드 실행")
    @Test
    void run() throws Exception {
        Class<Junit3Test> clazz = Junit3Test.class;
        Arrays.stream(clazz.getDeclaredMethods())
                .filter(c -> c.getName().startsWith("test"))
                .forEach(c -> {
                    try {
                        c.invoke(new Junit3Test());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
    }
}
