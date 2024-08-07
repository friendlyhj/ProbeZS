package youyihj.probezs.tree;

import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.member.ParameterData;
import youyihj.probezs.util.IndentStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author youyihj
 */
public class ZenConstructorNode extends ZenExecutableNode implements IHasImportMembers {
    private final List<ZenParameterNode> parameters;

    public ZenConstructorNode(List<ZenParameterNode> parameters) {
        this.parameters = parameters;
    }

    public static ZenConstructorNode read(ExecutableData constructor, ZenClassTree tree) {
        List<ZenParameterNode> parameterNodes = new ArrayList<>(constructor.getParameterCount());
        ParameterData[] parameters = constructor.getParameters();
        for (int i = 0; i < constructor.getParameterCount(); i++) {
            parameterNodes.add(ZenParameterNode.read(constructor, i, parameters[i], tree));
        }
        return new ZenConstructorNode(parameterNodes);
    }

    @Override
    public void fillImportMembers(Set<ZenClassNode> members) {
        for (ZenParameterNode parameter : parameters) {
            parameter.fillImportMembers(members);
        }
    }

    @Override
    protected boolean existed() {
        return true;
    }

    @Override
    protected void writeModifiersAndName(IndentStringBuilder sb) {
        sb.append("zenConstructor");
    }

    @Override
    protected List<ZenParameterNode> getParameters() {
        return parameters;
    }

    @Override
    protected JavaTypeMirror.Result getReturnType() {
        return null;
    }
}
