package youyihj.probezs.tree.primitive;

import com.google.gson.reflect.TypeToken;
import stanhebben.zenscript.type.ZenTypeIntRange;
import youyihj.probezs.tree.ZenClassNode;
import youyihj.probezs.tree.ZenClassTree;
import youyihj.probezs.tree.ZenMemberNode;
import youyihj.probezs.tree.ZenPropertyNode;

import java.util.Collections;
import java.util.List;

/**
 * @author youyihj
 */
public class ZenIntRangeNode extends ZenClassNode {
    public ZenIntRangeNode(ZenClassTree tree) {
        super(ZenTypeIntRange.INTRANGE.getName(), tree);
        properties.put("from", new ZenPropertyNode(tree.createLazyClassNode(int.class), "from"));
        properties.put("to", new ZenPropertyNode(tree.createLazyClassNode(int.class), "to"));
        ZenMemberNode iteratorMember = new ZenMemberNode(
                "iterator", tree.createLazyClassNode(new TypeToken<List<Integer>>() {}.getType()), Collections.emptyList(), false);
        iteratorMember.addAnnotation("foreach");
        iteratorMember.addAnnotation("hidden");
        members.add(iteratorMember);
    }
}
