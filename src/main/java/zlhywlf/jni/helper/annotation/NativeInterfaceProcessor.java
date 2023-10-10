package zlhywlf.jni.helper.annotation;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author zlhywlf (tommietanghao@zlhywlf.onmicrosoft.com)
 */
@SupportedAnnotationTypes("zlhywlf.jni.helper.annotation.NativeInterface")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class NativeInterfaceProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

}
