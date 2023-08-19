package youyihj.probezs.tree.primitive;

import youyihj.probezs.tree.ZenClassNode;
import youyihj.probezs.tree.ZenClassTree;
import youyihj.probezs.tree.ZenMemberNode;
import youyihj.probezs.tree.ZenParameterNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author youyihj
 */
public class ZenStringNode extends ZenClassNode implements IPrimitiveType {
    public ZenStringNode(ZenClassTree tree) {
        super("string", tree);
//        casterClasses.add(tree.createLazyClassNode(boolean.class));
//        casterClasses.add(tree.createLazyClassNode(byte.class));
//        casterClasses.add(tree.createLazyClassNode(short.class));
//        casterClasses.add(tree.createLazyClassNode(long.class));
//        casterClasses.add(tree.createLazyClassNode(float.class));
//        casterClasses.add(tree.createLazyClassNode(double.class));
        readJavaMethods(tree);
    }

    private void readJavaMethods(ZenClassTree tree) {
        try {
            readMethod:
            for (Method method : String.class.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
                    List<ZenParameterNode> parameterNodes = new ArrayList<>(method.getParameterCount());
                    Parameter[] parameters = method.getParameters();
                    if (convertType(method.getGenericReturnType()) == null) continue;
                    for (int i = 0; i < method.getParameterCount(); i++) {
                        Parameter parameter = parameters[i];
                        Type convertType = convertType(parameter.getParameterizedType());
                        if (convertType == null) continue readMethod;
                        parameterNodes.add(ZenParameterNode.read(method, i, parameter, tree));
                    }
                    ZenMemberNode zenMemberNode = new ZenMemberNode(method.getName(), tree.createLazyClassNode(method.getGenericReturnType()), parameterNodes, false);
                    members.add(zenMemberNode);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Type convertType(Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isPrimitive() && clazz != char.class) {
                return clazz;
            }
            if (clazz == CharSequence.class || clazz == String.class) {
                return String.class;
            }
            return null;
        }
        return type;
    }
}
