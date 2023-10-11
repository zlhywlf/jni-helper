package zlhywlf.jni.helper.core;

import com.sun.tools.javac.code.Type;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zlhywlf (tommietanghao@zlhywlf.onmicrosoft.com)
 */
@Getter
public class MethodInfo {

    @Setter
    private String name;
    private String descriptor;
    private final List<String> exceptions = new ArrayList<>();

    public MethodInfo(String name, Type.MethodType m) {
        setName(name);
        setDescriptor(m);
        setExceptions(m);
    }

    public void setDescriptor(Type.MethodType m) {
        StringBuilder builder = new StringBuilder("(");
        List<com.sun.tools.javac.code.Type> typeArguments = m.getParameterTypes();
        typeArguments.forEach(t -> builder.append(TypeUtil.transJavacTypeToAsmType(t).getDescriptor()));
        builder.append(")");
        builder.append(TypeUtil.transJavacTypeToAsmType(m.getReturnType()));
        this.descriptor = builder.toString();
    }

    public void setExceptions(Type.MethodType m) {
        List<com.sun.tools.javac.code.Type> thrownTypes = m.getThrownTypes();
        thrownTypes.forEach(thrownType -> this.exceptions.add(TypeUtil.transJavacTypeToAsmType(thrownType).getInternalName()));
    }

}
