package youyihj.probezs.tree;

import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

        Parameter[] parameters = method.getParameters();
        List<ZenParameterNode> parameterNodes = new ArrayList<>(method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            parameterNodes.add(ZenParameterNode.read(method, i, parameters[i], tree));
        }
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
