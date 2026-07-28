package youyihj.probezs.member;

import java.lang.reflect.Type;

/**
 * @author youyihj
 */
public interface ExecutableData extends AnnotatedMember {
    String getName();

    Class<?> getDeclaredClass();

    Class<?>[] getParameterTypes();

    int getParameterCount();

    int getModifiers();

    Type getReturnType();

    ParameterData[] getParameters();
}
