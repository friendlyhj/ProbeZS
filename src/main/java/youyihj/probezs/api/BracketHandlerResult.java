package youyihj.probezs.api;

import java.util.Map;

/**
 * @author youyihj
 */
public class BracketHandlerResult {
    private String type;
    private Map<String, String> extras;

    public BracketHandlerResult(String type, Map<String, String> extras) {
        this.type = type;
        this.extras = extras;
    }

    public BracketHandlerResult() {
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExtras(Map<String, String> extras) {
        this.extras = extras;
    }
}
