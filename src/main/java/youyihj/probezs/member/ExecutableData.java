package youyihj.probezs.member;

import java.lang.reflect.Type;

/**
 * @author youyihj
 */
public interface ExecutableData extends AnnotationMember {
    String getName();

    Class<?> getDecalredClass();

    Class<?>[] getParameterTypes();

    int getParameterCount();

    int getModifiers();

    Type getReturnType();

    ParameterData[] getParameters();
}
