package youyihj.probezs.member.reflection;

import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.member.FieldData;
import youyihj.probezs.member.MemberFactory;
import youyihj.probezs.member.ParameterData;
import youyihj.probezs.util.Arrays;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * @author youyihj
 */
public class ReflectionMemberFactory implements MemberFactory {
    @Override
    public FieldData[] getFields(Class<?> clazz) {
        return Arrays.map(clazz.getDeclaredFields(), FieldData.class, JavaField::new);
    }

    @Override
    public ExecutableData[] getMethods(Class<?> clazz) {
        return Arrays.map(clazz.getDeclaredMethods(), ExecutableData.class, JavaExecutable::new);
    }

    @Override
    public ExecutableData[] getConstructors(Class<?> clazz) {
        return Arrays.map(clazz.getDeclaredConstructors(), ExecutableData.class, JavaExecutable::new);
    }

    @Override
    public ExecutableData reflect(Executable executable) {
        return new JavaExecutable(executable);
    }

    @Override
    public FieldData reflect(Field field) {
        return new JavaField(field);
    }

    @Override
    public ParameterData reflect(Parameter parameter) {
        return new JavaParameter(parameter);
    }
}
