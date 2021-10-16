package reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Set;

class Junit4TestRunner {

    @DisplayName("Junit4Test에서 @MyTest 애노테이션이 있는 메소드 실행")
    @Test
    void run() throws Exception {
        Class<Junit4Test> clazz = Junit4Test.class;
        Set<Method> methods = ReflectionUtils.getMethods(clazz, method -> method.isAnnotationPresent(MyTest.class));
        Junit4Test instance = clazz.getConstructor().newInstance();
        for (Method method : methods) {
            method.invoke(instance);
        }
    }
}
