package youyihj.probezs.tree;

import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author youyihj
 */
public class ZenConstructorNode extends ZenExecutableNode implements IZenDumpable, IHasImportMembers {
    private final List<ZenParameterNode> parameters;

    public ZenConstructorNode(List<ZenParameterNode> parameters) {
        this.parameters = parameters;
    }

    public static ZenConstructorNode read(Constructor<?> constructor, ZenClassTree tree) {
        List<ZenParameterNode> parameterNodes = new ArrayList<>(constructor.getParameterCount());
        Parameter[] parameters = constructor.getParameters();
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
    public void toZenScript(IndentStringBuilder sb) {
        partialDump(sb, "zenConstructor", parameters, null);
    }
}
