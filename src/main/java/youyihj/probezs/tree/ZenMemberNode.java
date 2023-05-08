package youyihj.probezs.tree;

import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenMethodStatic;
import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author youyihj
 */
public class ZenMemberNode {
    private final String name;
    private final LazyZenClassNode returnType;
    private final List<ZenParameterNode> parameters;
    private final boolean isStatic;
    private String comment;

    public ZenMemberNode(String name, LazyZenClassNode returnType, List<ZenParameterNode> parameters, boolean isStatic) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.isStatic = isStatic;
    }

    public static ZenMemberNode read(Method method, ZenClassTree tree, boolean isClass) {
        if (isClass && method.isAnnotationPresent(ZenMethod.class)) {
            String name = method.getAnnotation(ZenMethod.class).value();
            if (name.isEmpty()) {
                name = method.getName();
            }
            return readInternal(method, tree, name, Modifier.isStatic(method.getModifiers()), false);
        }
        if (!isClass) {
            if (method.isAnnotationPresent(ZenMethod.class)) {
                String name = method.getAnnotation(ZenMethod.class).value();
                if (name.isEmpty()) {
                    name = method.getName();
                }
                return readInternal(method, tree, name, false, true);
            }
            if (method.isAnnotationPresent(ZenMethodStatic.class)) {
                String name = method.getAnnotation(ZenMethodStatic.class).value();
                if (name.isEmpty()) {
                    name = method.getName();
                }
                return readInternal(method, tree, name, true, false);
            }
        }
        return null;
    }

    private static ZenMemberNode readInternal(Method method, ZenClassTree tree, String name, boolean isStatic, boolean expansion) {
        List<ZenParameterNode> parameterNodes = new ArrayList<>(method.getParameterCount());
        int startIndex = expansion ? 1 : 0;
        Parameter[] parameters = method.getParameters();
        for (int i = startIndex; i < method.getParameterCount(); i++) {
            parameterNodes.add(ZenParameterNode.read(parameters[i], tree));
        }
        return new ZenMemberNode(name, tree.createLazyClassNode(method.getGenericReturnType()), parameterNodes, isStatic);
    }

    public void toZenScript(IndentStringBuilder sb) {
        if (returnType.isExisted()) {
            if (isStatic) {
                sb.append("static ");
            }
            sb.append("function ").append(name).append("(");
            Iterator<ZenParameterNode> iterator = parameters.iterator();
            while (iterator.hasNext()) {
                iterator.next().toZenScript(sb);
                if (iterator.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(")")
                    .append(" as ")
                    .append(returnType.get().getName())
                    .append(" {")
                    .push()
                    .append("//")
                    .append(comment != null ? comment : "...")
                    .pop()
                    .append("}");
        }
    }
}
