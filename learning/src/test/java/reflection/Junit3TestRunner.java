package reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Set;

class Junit3TestRunner {

    @DisplayName("Junit3Test에서 test로 시작하는 메소드 실행")
    @Test
    void run() throws Exception {
        Class<Junit3Test> clazz = Junit3Test.class;
        Set<Method> methods = ReflectionUtils.getMethods(clazz, method -> method.getName().startsWith("test"));
        Junit3Test instance = clazz.getConstructor().newInstance();
        for (Method method : methods) {
            method.invoke(instance);
        }
    }
}
