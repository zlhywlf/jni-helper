package zlhywlf.jni.helper.core;

import jdk.internal.org.objectweb.asm.Type;

/**
 * @author zlhywlf (tommietanghao@zlhywlf.onmicrosoft.com)
 */
public class TypeUtil {

    /**
     * 类型转换
     *
     * @param javacType com.sun.tools.javac.code.Type
     * @return Type
     */
    public static Type transJavacTypeToAsmType(com.sun.tools.javac.code.Type javacType) {
        return transJavacTypeToAsmType(javacType.toString());
    }

    /**
     * 类型转换
     *
     * @param javacTypeName String
     * @return Type
     */
    public static Type transJavacTypeToAsmType(String javacTypeName) {
        boolean isArr = javacTypeName.contains("[]");
        javacTypeName = isArr ? javacTypeName.replace("[]", "") : javacTypeName;
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
        return isArr ? Type.getType("[" + asmType.getDescriptor()) : asmType;
    }

}
