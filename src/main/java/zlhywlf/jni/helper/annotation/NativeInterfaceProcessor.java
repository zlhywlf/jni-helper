package zlhywlf.jni.helper.annotation;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

/**
 * @author zlhywlf (tommietanghao@zlhywlf.onmicrosoft.com)
 */
@SupportedAnnotationTypes("zlhywlf.jni.helper.annotation.NativeInterface")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class NativeInterfaceProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> interSets = roundEnv.getElementsAnnotatedWith(NativeInterface.class);
        interSets.forEach(inter -> {
            if (inter.getKind() != ElementKind.INTERFACE) {
                throw new RuntimeException(String.format("注解 @NativeInterface 只用于接口! [%s] 类型是 [%s]", inter, inter.getKind()));
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            classWriter.visit(V11, ACC_PUBLIC | ACC_SUPER, "zlhywlf/jni/helper/annotation/ExampleImpl", null, "java/lang/Object", new String[]{});

            // 无参构造
            MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();

            // 接口实现类组装完毕
            classWriter.visitEnd();

            // 输出字节文件
            try {
                JavaFileObject source = processingEnv.getFiler().createClassFile("zlhywlf.jni.helper.annotation.ExampleImpl");
                OutputStream out = source.openOutputStream();
                out.write(classWriter.toByteArray());
                out.flush();
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return true;
    }

}
