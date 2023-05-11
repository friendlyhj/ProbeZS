package youyihj.probezs.tree;

import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class ZenGlobalMethodNode implements IZenDumpable {
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
        List<ZenParameterNode> parameterNodes = Arrays.stream(method.getParameters())
                .map(it -> ZenParameterNode.read(it, tree))
                .collect(Collectors.toList());
        return new ZenGlobalMethodNode(name, returnType, parameterNodes);
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        sb.append("global ")
                .append(name)
                .append(" as function(");
        Iterator<ZenParameterNode> iterator = parameters.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next().getType().get().getName());
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(")")
                .append(returnType.get().getName())
                .append(" = function(");
        iterator = parameters.iterator();
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
                .append("// ...")
                .pop()
                .append("};");
    }
}
