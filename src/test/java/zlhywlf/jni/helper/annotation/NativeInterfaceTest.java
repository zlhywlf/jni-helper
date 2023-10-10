package zlhywlf.jni.helper.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.tools.ToolProvider;

/**
 * @author zlhywlf (tommietanghao@zlhywlf.onmicrosoft.com)
 */
public class NativeInterfaceTest {

    @Test
    void compileTest() {
        int ret = ToolProvider.getSystemJavaCompiler().run(null, null, null, new String[]{"-d", "target/test-classes", "src/test/java/zlhywlf/jni/helper/annotation/NativeInterfaceTest.java"});
        Assertions.assertEquals(0, ret);
    }

}

@NativeInterface
interface Example {
}