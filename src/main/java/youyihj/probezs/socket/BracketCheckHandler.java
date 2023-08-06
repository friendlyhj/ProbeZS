package youyihj.probezs.socket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import youyihj.probezs.api.BracketHandlerResult;
import youyihj.probezs.bracket.BracketHandlerCaller;

import java.util.Map;

/**
 * @author youyihj
 */
public class BracketCheckHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Gson GSON = new Gson();
    private static final JsonParser parser = new JsonParser();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) {
        if (msg instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) msg).text();
            JsonObject jsonObject = parser.parse(text).getAsJsonObject();
            String content = jsonObject.get("content").getAsString();
            boolean requireExtras = jsonObject.get("requireExtras").getAsBoolean();
            String json = GSON.toJson(outputJson(BracketHandlerCaller.INSTANCE.query(content, requireExtras)));
            ctx.writeAndFlush(new TextWebSocketFrame(json));
        } else if (msg instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame());
        }
    }

    public static JsonObject outputJson(BracketHandlerResult result) {
        JsonObject jsonObject = new JsonObject();
        if (result == null || result.getType() == null) {
            jsonObject.addProperty("type", "null");
            jsonObject.add("extra", new JsonObject());
        } else {
            jsonObject.addProperty("type", result.getType());
            jsonObject.add("extras", GSON.toJsonTree(result.getExtras(), new TypeToken<Map<String, String>>(){}.getType()));
        }
        return jsonObject;
    }
}
