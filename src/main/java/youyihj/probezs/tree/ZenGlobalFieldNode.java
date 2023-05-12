package youyihj.probezs.tree;

import youyihj.probezs.util.IndentStringBuilder;

/**
 * @author youyihj
 */
public class ZenGlobalFieldNode implements IZenDumpable {
    private final String name;
    private final LazyZenClassNode type;

    public ZenGlobalFieldNode(String name, LazyZenClassNode type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
//        if (type.isExisted()) {
            sb.append("global ").append(name).append(" as ").append(type.get().getName()).append(" = null;");
//        }
    }
}
