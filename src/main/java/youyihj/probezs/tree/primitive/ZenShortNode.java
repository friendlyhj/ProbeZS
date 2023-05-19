package youyihj.probezs.tree.primitive;

import youyihj.probezs.tree.ZenClassNode;
import youyihj.probezs.tree.ZenClassTree;

/**
 * @author youyihj
 */
public class ZenShortNode extends ZenClassNode {
    public ZenShortNode(ZenClassTree tree) {
        super("short", tree);
        casterClasses.add(tree.createLazyClassNode(byte.class));
        casterClasses.add(tree.createLazyClassNode(int.class));
        casterClasses.add(tree.createLazyClassNode(long.class));
        casterClasses.add(tree.createLazyClassNode(float.class));
        casterClasses.add(tree.createLazyClassNode(double.class));
        casterClasses.add(tree.createLazyClassNode(String.class));
    }
}
