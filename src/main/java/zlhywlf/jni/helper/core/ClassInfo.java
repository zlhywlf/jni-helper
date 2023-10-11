package zlhywlf.jni.helper.core;

import jdk.internal.org.objectweb.asm.Type;
import lombok.Getter;
import lombok.Setter;
import zlhywlf.jni.helper.annotation.NativeInterface;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zlhywlf (tommietanghao@zlhywlf.onmicrosoft.com)
 */
@Getter
@Setter
public class ClassInfo {

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

    /**
     * 方法
     */
    private List<MethodInfo> methodInfos = new ArrayList<>();

    public ClassInfo(Element inter) {
        NativeInterface interAnno = inter.getAnnotation(NativeInterface.class);
        String interName = inter.getSimpleName().toString();
        String interFullyQualifiedName = inter.getEnclosingElement() + "." + interName;
        String name = interAnno.value();
        Type interAsmType = TypeUtil.transJavacTypeToAsmType(interFullyQualifiedName);
        String interAsmTypeName = interAsmType.getInternalName();
        String suffix = interAnno.suffix();
        setLibName(name.isEmpty() ? interName : name);
        setClsAnnotations(interAnno.annotations());
        setInterAsmName(interAsmTypeName);
        setClsAsmName(interAsmTypeName + suffix);
        setClsFullyQualifiedName(interAsmType.getClassName() + suffix);
        setMethodInfos(inter.getEnclosedElements());
    }

    public void setMethodInfos(List<? extends Element> methods) {
        methods.forEach(method -> {
            TypeMirror asType = method.asType();
            if (asType instanceof com.sun.tools.javac.code.Type.MethodType m) {
                this.methodInfos.add(new MethodInfo(method.getSimpleName().toString(), m));
            }
        });
    }

    /**
     * jni 链接配置
     *
     * @return String
     */
    public String getConfig() {
        StringBuilder classInfoBuilder = new StringBuilder("{");
        StringBuilder methodsInfoBuilder = new StringBuilder("\"" + Constant.JNI_METHOD + "\": [");
        int size = methodInfos.size();
        for (int i = 0; i < size; i++) {
            MethodInfo methodInfo = methodInfos.get(i);
            methodsInfoBuilder.append("{");
            methodsInfoBuilder.append("\"" + Constant.JNI_ID + "\":");
            methodsInfoBuilder.append(i);
            methodsInfoBuilder.append(",");
            methodsInfoBuilder.append("\"" + Constant.JNI_NAME + "\":\"");
            methodsInfoBuilder.append(methodInfo.getName());
            methodsInfoBuilder.append("\",");
            methodsInfoBuilder.append("\"" + Constant.JNI_DES + "\":\"");
            methodsInfoBuilder.append(methodInfo.getDescriptor());
            methodsInfoBuilder.append("\"");
            methodsInfoBuilder.append("},");
        }
        methodsInfoBuilder.deleteCharAt(methodsInfoBuilder.lastIndexOf(","));
        methodsInfoBuilder.append("]");
        classInfoBuilder.append(methodsInfoBuilder);
        classInfoBuilder.append(",\"" + Constant.JNI_CLAZZ + "\": \"");
        classInfoBuilder.append(clsAsmName);
        classInfoBuilder.append("\"");
        classInfoBuilder.append("}");
        return classInfoBuilder.toString();
    }

}
