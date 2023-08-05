package youyihj.probezs.socket;

import youyihj.probezs.api.BracketHandlerResult;
import youyihj.probezs.api.IBracketHandlerCaller;

/**
 * @author youyihj
 */
public class SerializableBracketHandlerCaller implements IBracketHandlerCaller {
    private final IBracketHandlerCaller caller;

    public SerializableBracketHandlerCaller(IBracketHandlerCaller caller) {
        this.caller = caller;
    }

    @Override
    public BracketHandlerResult call(String content) {
        BracketHandlerResult result = caller.call(content);
        return new BracketHandlerResult(result.getType(), result.getExtras());
    }
}
