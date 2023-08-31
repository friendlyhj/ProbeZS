package youyihj.probezs.member.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.member.FieldData;
import youyihj.probezs.member.MemberFactory;
import youyihj.probezs.member.ParameterData;
import youyihj.probezs.member.reflection.JavaExecutable;
import youyihj.probezs.member.reflection.JavaField;
import youyihj.probezs.member.reflection.JavaParameter;
import youyihj.probezs.util.Arrays;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class ASMMemberFactory implements MemberFactory {
    private final Set<String> classAnnotationFilter;
    private final Map<String, ClassNode> classes = new HashMap<>();

    // double supplier to avoid early class loading
    private final Supplier<Supplier<ClassLoader>> classLoader;
    private final TypeResolver typeResolver;

    public ASMMemberFactory(Set<String> classAnnotationFilter, Supplier<Supplier<ClassLoader>> classLoader) {
        this.classAnnotationFilter = classAnnotationFilter;
        this.classLoader = classLoader;
        this.typeResolver = new TypeResolver(this);
    }

    public void putClassNode(ClassNode classNode) {
        if (classNode.visibleAnnotations != null) {
            for (AnnotationNode visibleAnnotation : classNode.visibleAnnotations) {
                for (String s : classAnnotationFilter) {
                    if (visibleAnnotation.desc.contains(s)) {
                        classes.put(classNode.name, classNode);
                    }
                }
            }
        }
    }

    @Override
    public FieldData[] getFields(Class<?> clazz) {
        ClassNode classNode = classes.get(Type.getInternalName(clazz));
        if (classNode != null) {
            return Arrays.map(classNode.fields.toArray(new FieldNode[0]), FieldData.class, it -> new ASMField(it, this, clazz));
        }
        return new FieldData[0];
    }

    @Override
    public ExecutableData[] getMethods(Class<?> clazz) {
        return getExecutables(clazz, false);
    }

    @Override
    public ExecutableData[] getConstructors(Class<?> clazz) {
        return getExecutables(clazz, true);
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

    public ClassLoader getClassLoader() {
        return classLoader.get().get();
    }

    TypeResolver getTypeDescResolver() {
        return typeResolver;
    }

    private ExecutableData[] getExecutables(Class<?> clazz, boolean constructor) {
        ClassNode classNode = classes.get(Type.getInternalName(clazz));
        Predicate<MethodNode> isConstructor = it -> "<init>".equals(it.name);
        if (classNode != null) {
            return classNode.methods.stream()
                    .filter(constructor ? isConstructor : isConstructor.negate())
                    .map(it -> new ASMMethod(it, this, clazz))
                    .toArray(ExecutableData[]::new);
        }
        return new ExecutableData[0];
    }
}
