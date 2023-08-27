package youyihj.probezs.member;

import com.google.common.base.Suppliers;
import youyihj.probezs.member.reflection.ReflectionMemberFactory;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.function.Supplier;

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

    Supplier<MemberFactory> DEFAULT = Suppliers.memoize(ReflectionMemberFactory::new);

    static MemberFactory getDefault() {
        return DEFAULT.get();
    }
}
