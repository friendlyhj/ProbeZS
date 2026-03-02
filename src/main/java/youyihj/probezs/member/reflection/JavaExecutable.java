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
        return executable instanceof Method ? ((Method) executable).getGenericReturnType() : getDecalredClass();
    }

    @Override
    public ParameterData[] getParameters() {
        try {
            return Arrays.map(executable.getParameters(), ParameterData.class, JavaParameter::new);
        } catch (MalformedParametersException e) {
            Class<?>[] parameterTypes = getParameterTypes();
            Type[] genericParameterTypes = executable.getGenericParameterTypes();
            Annotation[][] parameterAnnotations = executable.getParameterAnnotations();
            boolean isVarArgs = executable.isVarArgs();
            ParameterData[] parameters = new ParameterData[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameters[i] = new JavaParameterBackup(i, parameterTypes[i], genericParameterTypes[i], parameterAnnotations[i], isVarArgs && i == parameterTypes.length - 1);
            }
            return parameters;
        }
    }
}
