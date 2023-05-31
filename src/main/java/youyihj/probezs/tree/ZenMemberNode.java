package youyihj.probezs.tree;

import stanhebben.zenscript.annotations.ZenCaster;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenMethodStatic;
import stanhebben.zenscript.annotations.ZenOperator;
import youyihj.probezs.docs.ParameterNameMappings;
import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class ZenMemberNode implements IZenDumpable {
    private final String name;
    private final LazyZenClassNode returnType;
    private final List<ZenParameterNode> parameters;
    private final boolean isStatic;
    private final ZenAnnotationNode annotationNode = new ZenAnnotationNode();
    private final Supplier<String> returnTypeNameSupplier;

    public ZenMemberNode(String name, LazyZenClassNode returnType, List<ZenParameterNode> parameters, boolean isStatic) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.isStatic = isStatic;
        this.returnTypeNameSupplier = () -> returnType.get().getName();
    }

    public ZenMemberNode(String name, String returnType, List<ZenParameterNode> parameters, boolean isStatic) {
        this.name = name;
        this.returnType = null;
        this.parameters = parameters;
        this.isStatic = isStatic;
        this.returnTypeNameSupplier = () -> returnType;
    }

    public static ZenMemberNode read(Method method, ZenClassTree tree, boolean isClass) {
        if (method.isAnnotationPresent(ZenCaster.class)) {
            ZenMethod zenMethod = method.getAnnotation(ZenMethod.class);
            String name = method.getName();
            if (zenMethod != null && !zenMethod.value().isEmpty()) {
                name = zenMethod.value();
            }
            ZenMemberNode memberNode = readInternal(method, tree, name, false, !isClass);
            memberNode.addAnnotation("caster");
            if (zenMethod == null) {
                memberNode.addAnnotation("hidden");
            }
            return memberNode;
        }
        if (method.isAnnotationPresent(ZenOperator.class)) {
            ZenMethod zenMethod = method.getAnnotation(ZenMethod.class);
            String name = method.getName();
            if (zenMethod != null && !zenMethod.value().isEmpty()) {
                name = zenMethod.value();
            }
            ZenMemberNode memberNode = readInternal(method, tree, name, false, !isClass);
            memberNode.addAnnotation("operator", method.getAnnotation(ZenOperator.class).value().name());
            if (zenMethod == null) {
                memberNode.addAnnotation("hidden");
            }
            return memberNode;
        }
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

        int startIndex = expansion ? 1 : 0;
        Parameter[] parameters = method.getParameters();
        List<String> parameterNames = ParameterNameMappings.find(method);
        List<ZenParameterNode> parameterNodes = new ArrayList<>(method.getParameterCount());
        if (parameterNames != null && parameterNames.size() == parameters.length) {
            for (int i = startIndex; i < method.getParameterCount(); i++) {
                parameterNodes.add(ZenParameterNode.read(parameterNames.get(i), parameters[i], tree));
            }
        } else {
            for (int i = startIndex; i < method.getParameterCount(); i++) {
                parameterNodes.add(ZenParameterNode.read(parameters[i], tree));
            }
        }
        ZenMemberNode zenMemberNode = new ZenMemberNode(name, tree.createLazyClassNode(method.getGenericReturnType()), parameterNodes, isStatic);
        if (method.isVarArgs()) {
            zenMemberNode.addAnnotation("varargs");
        }
        return zenMemberNode;
    }

    public void addAnnotation(String head) {
        this.annotationNode.add(head);
    }

    public void addAnnotation(String head, String value) {
        this.annotationNode.add(head, value);
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        if (returnType == null || returnType.isExisted()) {
            if (parameters.stream().map(ZenParameterNode::getType).allMatch(LazyZenClassNode::isExisted)) {
                annotationNode.toZenScript(sb);
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
                    .append(returnTypeNameSupplier.get())
                    .append(" {")
                    .push()
                    .append("//")
                    .append("...")
                    .pop()
                    .append("}");
            }
        }
    }
}
