package youyihj.probezs.member.reflection;

import youyihj.probezs.member.ParameterData;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * @author youyihj
 */
public class JavaParameterBackup implements ParameterData {
    private final int index;
    private final Class<?> type;
    private final Type genericType;
    private final Annotation[] annotations;
    private final boolean varargs;

    public JavaParameterBackup(int index, Class<?> type, Type genericType, Annotation[] annotations, boolean varargs) {
        this.index = index;
        this.type = type;
        this.genericType = genericType;
        this.annotations = annotations;
        this.varargs = varargs;
    }

    @Override
    public String getName() {
        return "arg" + index;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Type getGenericType() {
        return genericType;
    }

    @Override
    public boolean isVarargs() {
        return varargs;
    }

    @Override
    public <A extends Annotation> boolean isAnnotationPresent(Class<A> annotationClass) {
        return Arrays.stream(annotations).anyMatch(annotation -> annotation.annotationType().equals(annotationClass));
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return Arrays.stream(annotations)
                .filter(annotation -> annotation.annotationType().equals(annotationClass))
                .map(annotationClass::cast)
                .findFirst()
                .orElse(null);
    }
}
