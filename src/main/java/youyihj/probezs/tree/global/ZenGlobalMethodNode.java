package youyihj.probezs.tree.global;

import youyihj.probezs.tree.*;
import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author youyihj
 */
public class ZenGlobalMethodNode implements IZenDumpable, IHasImportMembers, Comparable<ZenGlobalMethodNode> {
    private final String name;
    private final LazyZenClassNode returnType;
    private final List<ZenParameterNode> parameters;

    public ZenGlobalMethodNode(String name, LazyZenClassNode returnType, List<ZenParameterNode> parameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public static ZenGlobalMethodNode read(String name, Method method, ZenClassTree tree) {
        LazyZenClassNode returnType = tree.createLazyClassNode(method.getGenericReturnType());

        Parameter[] parameters = method.getParameters();
        List<ZenParameterNode> parameterNodes = new ArrayList<>(method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            parameterNodes.add(ZenParameterNode.read(method, i, parameters[i], tree));
        }
        return new ZenGlobalMethodNode(name, returnType, parameterNodes);
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        sb.append("global function ")
                .append(name)
                .append("(");

        Iterator<ZenParameterNode> iterator = parameters.iterator();
        while (iterator.hasNext()) {
            iterator.next().toZenScript(sb);
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append(")")
                .append(" as ")
                .append(returnType.get().getQualifiedName())
                .append(";");
    }

    @Override
    public void fillImportMembers(Set<ZenClassNode> members) {
        members.addAll(returnType.get().getTypeVariables());
        for (ZenParameterNode parameter : parameters) {
            parameter.fillImportMembers(members);
        }
    }

    @Override
    public int compareTo(ZenGlobalMethodNode o) {
        return name.compareTo(o.name);
    }
}
