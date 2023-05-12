package youyihj.probezs.tree;

import java.lang.reflect.Type;

/**
 * @author youyihj
 */
public class LazyZenClassNode {
    private final Type type;
    private final ZenClassTree classTree;
    private ZenClassNode zenClass;

    private boolean init = false;
    private boolean existed = false;

    public LazyZenClassNode(Type type, ZenClassTree classTree) {
        this.type = type;
        this.classTree = classTree;
    }

    public ZenClassNode get() {
        if (init) {
            return zenClass;
        }
        throw new IllegalStateException();
    }

    public boolean isExisted() {
        if (init) {
            return existed;
        }
        throw new IllegalStateException();
    }

    void fresh() {
        init = true;
        zenClass = classTree.getZenClassNode(type);
        existed = zenClass != classTree.getUnknownClass();
    }
}
