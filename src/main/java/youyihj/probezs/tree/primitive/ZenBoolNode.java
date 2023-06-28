package youyihj.probezs.tree.primitive;

import youyihj.probezs.tree.ZenClassNode;
import youyihj.probezs.tree.ZenClassTree;

/**
 * @author youyihj
 */
public class ZenBoolNode extends ZenClassNode implements IPrimitiveType {
    public ZenBoolNode(ZenClassTree tree) {
        super("bool", tree);
//        casterClasses.add(tree.createLazyClassNode(String.class));
    }
}
