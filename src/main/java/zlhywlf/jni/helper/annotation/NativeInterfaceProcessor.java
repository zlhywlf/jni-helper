package zlhywlf.jni.helper.annotation;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

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

    /**
     * 动态库名称
     */
    private String libName;

    /**
     * 接口汇编名
     */
    private String interAsmName;

    /**
     * 类注解
     */
    private String[] clsAnnotations;

    /**
     * 类汇编名
     */
    private String clsAsmName;

    /**
     * 类全限定名
     */
    private String clsFullyQualifiedName;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> interSets = roundEnv.getElementsAnnotatedWith(NativeInterface.class);
        interSets.forEach(inter -> {
            if (inter.getKind() != ElementKind.INTERFACE) {
                throw new RuntimeException(String.format("注解 @NativeInterface 只用于接口! [%s] 类型是 [%s]", inter, inter.getKind()));
            }

            // 处理接口信息
            NativeInterface interAnno = inter.getAnnotation(NativeInterface.class);
            libName = interAnno.value();
            String interName = inter.getSimpleName().toString();
            String interFullyQualifiedName = inter.getEnclosingElement() + "." + interName;
            if (libName.isEmpty()) {
                libName = interName;
            }
            clsAnnotations = interAnno.annotations();
            Type interAsmType = transJavacTypeToAsmType(interFullyQualifiedName);
            interAsmName = interAsmType.getInternalName();
            clsAsmName = interAsmName + interAnno.suffix();
            clsFullyQualifiedName = interAsmType.getClassName() + interAnno.suffix();

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            classWriter.visit(V11, ACC_PUBLIC | ACC_SUPER, clsAsmName, null, "java/lang/Object", new String[]{interAsmName});

            // 无参构造
            MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();

            // 静态代码块，加载动态库
            mv = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();

            mv.visitLdcInsn("clsAsmName");
            mv.visitLdcInsn(clsAsmName);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
            mv.visitInsn(POP);

            mv.visitLdcInsn(libName);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "loadLibrary", "(Ljava/lang/String;)V", false);

            mv.visitLdcInsn("clsAsmName");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "clearProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
            mv.visitInsn(POP);

            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 0);
            mv.visitEnd();

            // 添加注解
            AnnotationVisitor av;
            for (String annotation : clsAnnotations) {
                av = classWriter.visitAnnotation(transJavacTypeToAsmType(annotation).getDescriptor(), true);
                av.visitEnd();
            }

            // 接口实现类组装完毕
            classWriter.visitEnd();

            // 输出字节文件
            try {
                JavaFileObject source = processingEnv.getFiler().createClassFile(clsFullyQualifiedName);
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

    private Type transJavacTypeToAsmType(com.sun.tools.javac.code.Type javacType) {
        return transJavacTypeToAsmType(javacType.toString());
    }

    private Type transJavacTypeToAsmType(String javacTypeName) {
        boolean isArr = javacTypeName.contains("[]");
        if (isArr) {
            javacTypeName = javacTypeName.replace("[]", "");
        }
        Type asmType = switch (javacTypeName) {
            case "int" -> Type.INT_TYPE;
            case "double" -> Type.DOUBLE_TYPE;
            case "float" -> Type.FLOAT_TYPE;
            case "long" -> Type.LONG_TYPE;
            case "short" -> Type.SHORT_TYPE;
            case "char" -> Type.CHAR_TYPE;
            case "boolean" -> Type.BOOLEAN_TYPE;
            case "byte" -> Type.BYTE_TYPE;
            case "void" -> Type.VOID_TYPE;
            default -> Type.getObjectType(javacTypeName.replace(".", "/"));
        };
        if (isArr) {
            return Type.getType("[" + asmType.getDescriptor());
        }
        return asmType;
    }

}
