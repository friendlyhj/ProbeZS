package youyihj.probezs.socket.rpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * @author youyihj
 */
public class RpcRequest {
    private final String jsonrpc;
    private final String id;
    private final String method;
    private final JsonArray params;

    public RpcRequest(String jsonrpc, String id, String method, JsonArray params) {
        this.jsonrpc = jsonrpc;
        this.id = id;
        this.method = method;
        this.params = params;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public String getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public JsonArray getParams() {
        return params;
    }

    public RpcResponse response(JsonElement result) {
        RpcResponse rpcResponse = new RpcResponse(getJsonrpc(), getId());
        rpcResponse.setResult(result);
        return rpcResponse;
    }
}
