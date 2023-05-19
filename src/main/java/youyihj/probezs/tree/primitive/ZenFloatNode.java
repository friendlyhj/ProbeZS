package youyihj.probezs.tree.primitive;

import youyihj.probezs.tree.ZenClassNode;
import youyihj.probezs.tree.ZenClassTree;

/**
 * @author youyihj
 */
public class ZenFloatNode extends ZenClassNode {
    public ZenFloatNode(ZenClassTree tree) {
        super("float", tree);
        casterClasses.add(tree.createLazyClassNode(byte.class));
        casterClasses.add(tree.createLazyClassNode(short.class));
        casterClasses.add(tree.createLazyClassNode(int.class));
        casterClasses.add(tree.createLazyClassNode(long.class));
        casterClasses.add(tree.createLazyClassNode(double.class));
        casterClasses.add(tree.createLazyClassNode(String.class));
    }
}
