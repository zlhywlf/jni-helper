package zlhywlf.jni.helper.core;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

/**
 * @author zlhywlf (tommietanghao@zlhywlf.onmicrosoft.com)
 */
public class ClassBuilder {

    private final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    private final ClassInfo info;

    public ClassBuilder(ClassInfo info) {
        this.info = info;
    }

    /**
     * 添加注解
     */
    private void generateAnnotations() {
        AnnotationVisitor av;
        for (String annotation : info.getClsAnnotations()) {
            av = classWriter.visitAnnotation(TypeUtil.transJavacTypeToAsmType(annotation).getDescriptor(), true);
            av.visitEnd();
        }

    }

    /**
     * 生成静态代码块，完成动态库的加载
     */
    private void generateStaticBlock() {
        MethodVisitor mv = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        // 配置
        mv.visitLdcInsn(Constant.JNI_CONFIG);
        mv.visitLdcInsn(info.getConfig());
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitInsn(POP);
        // 加载动态库
        mv.visitLdcInsn(info.getLibName());
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "loadLibrary", "(Ljava/lang/String;)V", false);
        // 清理
        mv.visitLdcInsn(Constant.JNI_CONFIG);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "clearProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitInsn(POP);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 0);
        mv.visitEnd();
    }

    /**
     * 生成默认构造
     */
    private void generateDefaultConstructor() {
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /**
     * 生成方法
     */
    private void generateMethods() {
        info.getMethodInfos().forEach(methodInfo -> classWriter.visitMethod(ACC_PUBLIC | ACC_NATIVE, methodInfo.getName(), methodInfo.getDescriptor(), null, methodInfo.getExceptions().toArray(new String[0])).visitEnd());
    }

    public byte[] build() {
        classWriter.visit(V11, ACC_PUBLIC | ACC_SUPER, info.getClsAsmName(), null, "java/lang/Object", new String[]{info.getInterAsmName()});
        generateAnnotations();
        generateDefaultConstructor();
        generateStaticBlock();
        generateMethods();
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

}
