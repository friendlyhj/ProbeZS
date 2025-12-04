package youyihj.probezs.tree;

import youyihj.probezs.util.IndentStringBuilder;
import youyihj.zenutils.impl.member.ExecutableData;
import youyihj.zenutils.impl.member.TypeData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author youyihj
 */
public class ZenConstructorNode extends ZenExecutableNode implements ITypeNameContextAcceptor {
    private final List<ZenParameterNode> parameters;

    public ZenConstructorNode(List<ZenParameterNode> parameters) {
        this.parameters = parameters;
    }

    public static ZenConstructorNode read(ExecutableData constructor, ZenClassTree tree) {
        List<ZenParameterNode> parameterNodes = new ArrayList<>(constructor.parameterCount());
        List<TypeData> parameters = constructor.parameters();
        for (int i = 0; i < constructor.parameterCount(); i++) {
            parameterNodes.add(ZenParameterNode.read(constructor, i, parameters.get(i), tree));
        }
        return new ZenConstructorNode(parameterNodes);
    }

    @Override
    public void setMentionedTypes(TypeNameContext context) {
        for (ZenParameterNode parameter : parameters) {
            parameter.setMentionedTypes(context);
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
