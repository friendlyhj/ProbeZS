package youyihj.probezs.socket;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import crafttweaker.api.item.IItemStack;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import youyihj.probezs.bracket.BracketHandlerCaller;

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
            JsonElement jsonElement = parser.parse(text);
            String content = jsonElement.getAsJsonObject().get("content").getAsString();
            String json = GSON.toJson(outputJson(BracketHandlerCaller.call(content)));
            ctx.writeAndFlush(new TextWebSocketFrame(json));
        } else if (msg instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame());
        }
    }

    private JsonElement outputJson(BracketHandlerCaller.Result result) {
        JsonObject jsonObject = new JsonObject();
        if (result == null || result.getObject() == null) {
            jsonObject.addProperty("type", "null");
        } else {
            jsonObject.addProperty("type", result.getType().getName());
            JsonObject extra = new JsonObject();
            jsonObject.add("extra", extra);
            if (result.getObject() instanceof IItemStack) {
                extra.addProperty("name", ((IItemStack) result.getObject()).getDisplayName());
            }
        }
        return jsonObject;
    }
}
