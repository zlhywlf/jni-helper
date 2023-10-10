package zlhywlf.jni.helper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为接口生成本地方法实现，仅限用于接口
 *
 * @author zlhywlf (tommietanghao@zlhywlf.onmicrosoft.com)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface NativeInterface {

    /**
     * 动态库项目名称，默认为接口名
     * 动态库安装位置参考 System.getProperty("java.library.path")
     * 不同系统下动态库文件名参考 System.mapLibraryName("libName")
     */
    String value() default "";

    /**
     * 实现类额外注解，需要使用注解的全限定名
     */
    String[] annotations() default {};

    /**
     * 实现类后缀
     */
    String suffix() default "Impl";

}
