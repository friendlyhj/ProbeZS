package youyihj.probezs.socket;

import com.google.gson.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import youyihj.probezs.api.BracketHandlerResult;
import youyihj.probezs.bracket.BracketHandlerCaller;

import java.util.HashMap;
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

    public static JsonElement outputJson(BracketHandlerResult result) {
        Map<String, String> map = new HashMap<>();
        if (result.getType() != null) {
            map.put("type", result.getType());
        } else {
            map.put("_error", "");
        }
        map.putAll(result.getExtras());
        return GSON.toJsonTree(map);
    }
}
