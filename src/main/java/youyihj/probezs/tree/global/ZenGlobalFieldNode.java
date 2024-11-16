package youyihj.probezs.tree.global;

import youyihj.probezs.tree.ITypeNameContextAcceptor;
import youyihj.probezs.tree.IZenDumpable;
import youyihj.probezs.tree.JavaTypeMirror;
import youyihj.probezs.tree.TypeNameContext;
import youyihj.probezs.util.IndentStringBuilder;

/**
 * @author youyihj
 */
public class ZenGlobalFieldNode implements IZenDumpable, ITypeNameContextAcceptor, Comparable<ZenGlobalFieldNode> {
    private final String name;
    private final JavaTypeMirror type;

    public ZenGlobalFieldNode(String name, JavaTypeMirror type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public void toZenScript(IndentStringBuilder sb, TypeNameContext context) {
        sb.append("global ").append(name).append(" as ").append(context.getTypeName(type.get())).append(";");
    }

    @Override
    public void setMentionedTypes(TypeNameContext context) {
        context.addClasses(type.get().getTypeVariables());
    }

    @Override
    public int compareTo(ZenGlobalFieldNode o) {
        return name.compareTo(o.name);
    }
}
