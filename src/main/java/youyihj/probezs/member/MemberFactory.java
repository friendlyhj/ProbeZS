package youyihj.probezs.member;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * @author youyihj
 */
public interface MemberFactory {
    FieldData[] getFields(Class<?> clazz);

    ExecutableData[] getMethods(Class<?> clazz);

    ExecutableData[] getConstructors(Class<?> clazz);

    ExecutableData reflect(Executable executable);

    FieldData reflect(Field field);

    ParameterData reflect(Parameter parameter);
}
