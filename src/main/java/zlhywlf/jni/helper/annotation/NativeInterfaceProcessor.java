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
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            StringBuilder classInfoBuilder = new StringBuilder("{");
            StringBuilder methodsInfoBuilder = new StringBuilder("\"methods\": [");
            processingInfo(inter);
            processingMethods(classWriter, inter, methodsInfoBuilder);
            methodsInfoBuilder.append("]");
            classInfoBuilder.append(methodsInfoBuilder);
            classInfoBuilder.append(",\"clazz\": \"");
            classInfoBuilder.append(clsAsmName);
            classInfoBuilder.append("\"}");
            generateClass(classWriter, classInfoBuilder);
            writeClass(classWriter);
        });
        return true;
    }

    private void processingMethods(ClassWriter classWriter, Element inter, StringBuilder methodsInfoBuilder) {
        List<? extends Element> methods = inter.getEnclosedElements();
        for (int i = 0; i < methods.size(); i++) {
            Element interMethod = methods.get(i);
            TypeMirror asType = interMethod.asType();
            if (asType instanceof com.sun.tools.javac.code.Type.MethodType m) {
                // 处理方法描述
                StringBuilder builder = new StringBuilder("(");
                List<com.sun.tools.javac.code.Type> typeArguments = m.getParameterTypes();
                typeArguments.forEach(t -> builder.append(transJavacTypeToAsmType(t).getDescriptor()));
                builder.append(")");
                builder.append(transJavacTypeToAsmType(m.getReturnType()));
                // 处理异常
                List<com.sun.tools.javac.code.Type> thrownTypes = m.getThrownTypes();
                String[] exceptions = new String[thrownTypes.size()];
                for (int j = 0; j < exceptions.length; j++) {
                    exceptions[j] = transJavacTypeToAsmType(thrownTypes.get(j)).getInternalName();
                }
                String name = interMethod.getSimpleName().toString();
                String des = builder.toString();
                methodsInfoBuilder.append("{");
                methodsInfoBuilder.append("\"id\":\"");
                methodsInfoBuilder.append(i);
                methodsInfoBuilder.append("\",");
                methodsInfoBuilder.append("\"name\":\"");
                methodsInfoBuilder.append(name);
                methodsInfoBuilder.append("\",");
                methodsInfoBuilder.append("\"des\":\"");
                methodsInfoBuilder.append(des);
                methodsInfoBuilder.append("\"");
                methodsInfoBuilder.append("}");
                if (i < methods.size() - 1) {
                    methodsInfoBuilder.append(",");
                }
                // 生成方法
                classWriter.visitMethod(ACC_PUBLIC | ACC_NATIVE, name, des, null, exceptions).visitEnd();
            }
        }
    }

    /**
     * 处理接口与注解数据
     *
     * @param inter Element
     */
    private void processingInfo(Element inter) {
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
    }

    /**
     * 生成接口实现类
     *
     * @param classWriter ClassWriter
     */
    private void generateClass(ClassWriter classWriter, StringBuilder classInfoBuilder) {
        classWriter.visit(V11, ACC_PUBLIC | ACC_SUPER, clsAsmName, null, "java/lang/Object", new String[]{interAsmName});
        generateDefaultConstructor(classWriter);
        generateStaticBlock(classWriter, classInfoBuilder);
        // 添加注解
        AnnotationVisitor av;
        for (String annotation : clsAnnotations) {
            av = classWriter.visitAnnotation(transJavacTypeToAsmType(annotation).getDescriptor(), true);
            av.visitEnd();
        }
        classWriter.visitEnd();
    }

    /**
     * 生成默认构造函数
     *
     * @param classWriter ClassWriter
     */
    private void generateDefaultConstructor(ClassWriter classWriter) {
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /**
     * 生成静态代码块，完成动态库的加载
     *
     * @param classWriter ClassWriter
     */
    private void generateStaticBlock(ClassWriter classWriter, StringBuilder classInfoBuilder) {
        MethodVisitor mv = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        // 类名
        mv.visitLdcInsn("jni.config");
        mv.visitLdcInsn(classInfoBuilder.toString());
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitInsn(POP);
        // 加载动态库
        mv.visitLdcInsn(libName);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "loadLibrary", "(Ljava/lang/String;)V", false);
        // 清理
        mv.visitLdcInsn("jni.config");
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "clearProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitInsn(POP);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 0);
        mv.visitEnd();
    }

    /**
     * 输出类
     *
     * @param classWriter ClassWriter
     */
    private void writeClass(ClassWriter classWriter) {
        try {
            JavaFileObject source = processingEnv.getFiler().createClassFile(clsFullyQualifiedName);
            OutputStream out = source.openOutputStream();
            out.write(classWriter.toByteArray());
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 类型转换
     *
     * @param javacType com.sun.tools.javac.code.Type
     * @return Type
     */
    private Type transJavacTypeToAsmType(com.sun.tools.javac.code.Type javacType) {
        return transJavacTypeToAsmType(javacType.toString());
    }

    /**
     * 类型转换
     *
     * @param javacTypeName String
     * @return Type
     */
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
