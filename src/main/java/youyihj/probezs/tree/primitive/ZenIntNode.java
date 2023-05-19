package youyihj.probezs.tree.primitive;

import youyihj.probezs.tree.ZenClassNode;
import youyihj.probezs.tree.ZenClassTree;

/**
 * @author youyihj
 */
public class ZenIntNode extends ZenClassNode {
    public ZenIntNode(ZenClassTree tree) {
        super("int", tree);
        casterClasses.add(tree.createLazyClassNode(byte.class));
        casterClasses.add(tree.createLazyClassNode(short.class));
        casterClasses.add(tree.createLazyClassNode(long.class));
        casterClasses.add(tree.createLazyClassNode(float.class));
        casterClasses.add(tree.createLazyClassNode(double.class));
        casterClasses.add(tree.createLazyClassNode(String.class));
    }
}
