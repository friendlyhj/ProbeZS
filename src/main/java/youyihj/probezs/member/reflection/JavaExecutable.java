package youyihj.probezs.member.reflection;

import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.member.ParameterData;
import youyihj.probezs.util.Arrays;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

/**
 * @author youyihj
 */
public class JavaExecutable implements ExecutableData {
    private final Executable executable;

    public JavaExecutable(Executable executable) {
        this.executable = executable;
    }

    @Override
    public <A extends Annotation> boolean isAnnotationPresent(Class<A> annotationClass) {
        return executable.isAnnotationPresent(annotationClass);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return executable.getAnnotation(annotationClass);
    }

    @Override
    public String getName() {
        return executable instanceof Constructor ? "<init>" : executable.getName();
    }

    @Override
    public Class<?> getDecalredClass() {
        return executable.getDeclaringClass();
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return executable.getParameterTypes();
    }

    @Override
    public int getParameterCount() {
        return executable.getParameterCount();
    }

    @Override
    public int getModifiers() {
        return executable.getModifiers();
    }

    @Override
    public Type getReturnType() {
        return executable instanceof Method ? ((Method) executable).getReturnType() : getDecalredClass();
    }

    @Override
    public ParameterData[] getParameters() {
        return Arrays.map(executable.getParameters(), ParameterData.class, JavaParameter::new);
    }
}
