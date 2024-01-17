package youyihj.probezs.bracket;

import stanhebben.zenscript.type.ZenType;

/**
 * @author youyihj
 */
public class BracketHandlerResult {
    private final Object object;
    private final ZenType zenType;

    public BracketHandlerResult(Object object, ZenType zenType) {
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
