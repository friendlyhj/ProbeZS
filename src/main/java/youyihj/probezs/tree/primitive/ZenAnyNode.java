package youyihj.probezs.tree.primitive;

import youyihj.probezs.tree.ZenClassNode;
import youyihj.probezs.tree.ZenClassTree;

/**
 * @author youyihj
 */
public class ZenAnyNode extends ZenClassNode implements IPrimitiveType {
    public ZenAnyNode(ZenClassTree tree) {
        super("any", tree);
    }
}
