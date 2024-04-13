package youyihj.probezs.tree;

import youyihj.probezs.util.IndentStringBuilder;

import java.util.Iterator;
import java.util.List;

/**
 * @author youyihj
 */
public abstract class ZenExecutableNode implements IMaybeExpansionMember, IZenDumpable {
    private String expansionOwner;

    protected abstract boolean existed();

    protected abstract void writeModifiersAndName(IndentStringBuilder sb);

    protected abstract List<ZenParameterNode> getParameters();

    protected abstract JavaTypeMirror.Result getReturnType();

    @Override
    public final void toZenScript(IndentStringBuilder sb) {
        if (!existed()) return;
        if (expansionOwner != null) {
            sb.append("// expansion member from ").append(expansionOwner).nextLine();
        }
        writeModifiersAndName(sb);
        sb.append("(");
        Iterator<ZenParameterNode> iterator = getParameters().iterator();
        while (iterator.hasNext()) {
            iterator.next().toZenScript(sb);
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(")");
        JavaTypeMirror.Result returnType = getReturnType();
        if (returnType != null) {
            sb.append(" as ").append(returnType.getQualifiedName());
        }
        sb.append(";");
    }

    @Override
    public void setOwner(String owner) {
        this.expansionOwner = owner;
    }

    @Override
    public String getOwner() {
        return expansionOwner;
    }
}
