package youyihj.probezs.member.reflection;

import youyihj.probezs.member.FieldData;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author youyihj
 */
public class JavaField implements FieldData {
    private final Field field;

    public JavaField(Field field) {
        this.field = field;
    }

    @Override
    public <A extends Annotation> boolean isAnnotationPresent(Class<A> annotationClass) {
        return field.isAnnotationPresent(annotationClass);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return field.getAnnotation(annotationClass);
    }

    @Override
    public Class<?> getDecalredClass() {
        return field.getDeclaringClass();
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public int getModifiers() {
        return field.getModifiers();
    }

    @Override
    public Type getType() {
        return field.getGenericType();
    }
}
