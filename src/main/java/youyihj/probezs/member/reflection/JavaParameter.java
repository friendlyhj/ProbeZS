package youyihj.probezs.member.reflection;

import youyihj.probezs.member.ParameterData;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 * @author youyihj
 */
public class JavaParameter implements ParameterData {
    private final Parameter parameter;

    public JavaParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public <A extends Annotation> boolean isAnnotationPresent(Class<A> annotationClass) {
        return parameter.isAnnotationPresent(annotationClass);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return parameter.getAnnotation(annotationClass);
    }

    @Override
    public String getName() {
        return parameter.getName();
    }

    @Override
    public Class<?> getType() {
        return parameter.getType();
    }

    @Override
    public Type getGenericType() {
        return parameter.getParameterizedType();
    }

    @Override
    public boolean isVarargs() {
        return parameter.isVarArgs();
    }
}
