package reflection;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

class Junit4TestRunner {

    @Test
    void run() throws Exception {
        Class<Junit4Test> clazz = Junit4Test.class;

        for(Method method : clazz.getDeclaredMethods()){
            if(method.isAnnotationPresent(MyTest.class)){
                method.invoke(clazz.getConstructor().newInstance());
            }
        }
    }
}
