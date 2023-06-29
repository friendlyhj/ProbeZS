package youyihj.probezs.tree.global;

import youyihj.probezs.tree.IHasImportMembers;
import youyihj.probezs.tree.IZenDumpable;
import youyihj.probezs.tree.LazyZenClassNode;
import youyihj.probezs.tree.ZenClassNode;
import youyihj.probezs.util.IndentStringBuilder;

import java.util.Set;

/**
 * @author youyihj
 */
public class ZenGlobalFieldNode implements IZenDumpable, IHasImportMembers, Comparable<ZenGlobalFieldNode> {
    private final String name;
    private final LazyZenClassNode type;

    public ZenGlobalFieldNode(String name, LazyZenClassNode type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        sb.append("global ").append(name).append(" as ").append(type.get().getQualifiedName()).append(";");
    }

    @Override
    public void fillImportMembers(Set<ZenClassNode> members) {
        members.addAll(type.get().getTypeVariables());
    }

    @Override
    public int compareTo(ZenGlobalFieldNode o) {
        return name.compareTo(o.name);
    }
}
