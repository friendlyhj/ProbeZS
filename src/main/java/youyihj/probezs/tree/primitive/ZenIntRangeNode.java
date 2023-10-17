package youyihj.probezs.tree.primitive;

import com.google.gson.reflect.TypeToken;
import stanhebben.zenscript.type.ZenTypeIntRange;
import youyihj.probezs.tree.*;

import java.util.Collections;
import java.util.List;

/**
 * @author youyihj
 */
public class ZenIntRangeNode extends ZenClassNode {
    public ZenIntRangeNode(ZenClassTree tree) {
        super(ZenTypeIntRange.INTRANGE.getName(), tree);
        properties.put("from", new ZenPropertyNode(tree.createJavaTypeMirror(int.class), "from"));
        properties.put("to", new ZenPropertyNode(tree.createJavaTypeMirror(int.class), "to"));
        operators.put("for_in", new ZenOperatorNode(
                "for_in",
                Collections.emptyList(),
                tree.createJavaTypeMirror(new TypeToken<List<Integer>>() {}.getType())
        ));
    }
}
