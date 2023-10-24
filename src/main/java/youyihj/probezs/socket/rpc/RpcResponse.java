package youyihj.probezs.socket.rpc;

import com.google.gson.JsonElement;

/**
 * @author youyihj
 */
public class RpcResponse {
    private final String jsonrpc;
    private final String id;
    private JsonElement result;

    public RpcResponse(String jsonrpc, String id) {
        this.jsonrpc = jsonrpc;
        this.id = id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public String getId() {
        return id;
    }

    public JsonElement getResult() {
        return result;
    }

    public void setResult(JsonElement result) {
        this.result = result;
    }
}
