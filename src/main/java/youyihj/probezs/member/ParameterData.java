package youyihj.probezs.member;

import java.lang.reflect.Type;

/**
 * @author youyihj
 */
public interface ParameterData extends AnnotatedMember {
    String getName();

    Class<?> getType();

    Type getGenericType();

    boolean isVarargs();
}
