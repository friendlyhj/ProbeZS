package youyihj.probezs.tree.global;

import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.member.ParameterData;
import youyihj.probezs.tree.*;
import youyihj.probezs.util.IndentStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author youyihj
 */
public class ZenGlobalMethodNode extends ZenExecutableNode implements IZenDumpable, IHasImportMembers, Comparable<ZenGlobalMethodNode> {
    private final String name;
    private final LazyZenClassNode returnType;
    private final List<ZenParameterNode> parameters;

    public ZenGlobalMethodNode(String name, LazyZenClassNode returnType, List<ZenParameterNode> parameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public static ZenGlobalMethodNode read(String name, ExecutableData method, ZenClassTree tree) {
        LazyZenClassNode returnType = tree.createLazyClassNode(method.getReturnType());

        ParameterData[] parameters = method.getParameters();
        List<ZenParameterNode> parameterNodes = new ArrayList<>(method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            parameterNodes.add(ZenParameterNode.read(method, i, parameters[i], tree));
        }
        return new ZenGlobalMethodNode(name, returnType, parameterNodes);
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        sb.append("global function ");
        partialDump(sb, name, parameters, returnType.get());
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
