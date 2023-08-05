package youyihj.probezs.bracket;

import stanhebben.zenscript.type.ZenType;
import youyihj.probezs.api.BracketHandlerResult;

import java.util.HashMap;

/**
 * @author youyihj
 */
public class ZenBracketHandlerResult extends BracketHandlerResult {
    private final Object object;
    private final ZenType zenType;

    public ZenBracketHandlerResult(Object object, ZenType zenType) {
        super(zenType.getName(), new HashMap<>());
        this.object = object;
        this.zenType = zenType;
    }

    public Object getObject() {
        return object;
    }

    public ZenType getZenType() {
        return zenType;
    }
}
