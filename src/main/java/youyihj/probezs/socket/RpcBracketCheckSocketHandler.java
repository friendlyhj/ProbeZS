package youyihj.probezs.socket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import youyihj.probezs.api.BracketHandlerResult;
import youyihj.probezs.bracket.BracketHandlerCaller;

import java.nio.charset.StandardCharsets;

/**
 * @author youyihj
 */
public class RpcBracketCheckSocketHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final JsonParser parser = new JsonParser();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        // JSON-RPC over HTTP
        String httpRequest = msg.toString(StandardCharsets.UTF_8);
        String httpRequestBody = httpRequest.substring(httpRequest.lastIndexOf("\r\n"));
        JsonObject jsonRequest = ((JsonObject) parser.parse(httpRequestBody));
        if (jsonRequest.get("jsonrpc").getAsString().equals("2.0")) {
            String id = jsonRequest.get("id").getAsString();
            String method = jsonRequest.get("method").getAsString();
            if (method.equals("query")) {
                JsonArray params = jsonRequest.get("params").getAsJsonArray();
                BracketHandlerResult result = BracketHandlerCaller.INSTANCE.query(params.get(0).getAsString(), params.get(1).getAsBoolean());
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("jsonrpc", "2.0");
                if (!"null".equals(result.getType())) {
                    jsonResponse.add("result", BracketCheckHandler.outputJson(result));
                } else {
                    jsonResponse.add("result", new JsonPrimitive("_error"));
//                    JsonObject error = new JsonObject();
//                    error.addProperty("code", -10000);
//                    error.addProperty("message", "Unknown bracket handler");
//                    jsonResponse.add("error", error);
                }
                jsonResponse.addProperty("id", id);
                String json = jsonResponse.toString();
                byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
                ByteBuf buffer = Unpooled.buffer(0);
                buffer.writeCharSequence("Content-Length: " + jsonBytes.length + "\r\n", StandardCharsets.UTF_8);
                buffer.writeCharSequence("\r\n", StandardCharsets.UTF_8);
                buffer.writeBytes(jsonBytes);
                ctx.channel().writeAndFlush(buffer);
            }
        }
    }
}
