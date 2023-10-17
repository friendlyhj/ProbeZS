package youyihj.probezs.tree;

import youyihj.probezs.util.IndentStringBuilder;

import java.util.Iterator;
import java.util.List;

/**
 * @author youyihj
 */
public class ZenExecutableNode {
    protected void partialDump(IndentStringBuilder sb, String name, List<ZenParameterNode> parameters, JavaTypeMirror.Result returnType) {
        sb.append(name).append("(");
        Iterator<ZenParameterNode> iterator = parameters.iterator();
        while (iterator.hasNext()) {
            iterator.next().toZenScript(sb);
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(")");
        if (returnType != null) {
            sb.append(" as ").append(returnType.getQualifiedName());
        }
        sb.append(";");
    }
}
