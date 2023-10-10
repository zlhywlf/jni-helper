package zlhywlf.jni.helper.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.tools.ToolProvider;
import java.io.IOException;

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

@NativeInterface(annotations = {"java.lang.Deprecated"})
interface Example {

    void run(byte a0, short a1, int a2, long a3, float a4, double a5, char a6, boolean a7, String a8, String[] a9) throws IOException, ArrayIndexOutOfBoundsException;

    void exec(String a);

}