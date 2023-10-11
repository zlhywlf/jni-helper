package zlhywlf.jni.helper.annotation;

import zlhywlf.jni.helper.core.ClassBuilder;
import zlhywlf.jni.helper.core.ClassInfo;

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
            ClassInfo info = new ClassInfo(inter);
            try {
                JavaFileObject source = processingEnv.getFiler().createClassFile(info.getClsFullyQualifiedName());
                OutputStream out = source.openOutputStream();
                out.write(new ClassBuilder(info).build());
                out.flush();
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return true;
    }

}
