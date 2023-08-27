package youyihj.probezs.member;

import java.lang.reflect.Type;

/**
 * @author youyihj
 */
public interface FieldData extends AnnotationMember {
    Class<?> getDecalredClass();

    String getName();

    int getModifiers();

    Type getType();
}
