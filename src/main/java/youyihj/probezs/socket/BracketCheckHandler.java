package youyihj.probezs.socket;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import crafttweaker.api.item.IItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import youyihj.probezs.bracket.BracketHandlerCaller;

import java.nio.charset.StandardCharsets;

/**
 * @author youyihj
 */
public class BracketCheckHandler extends ChannelInboundHandlerAdapter {
    private static final Gson GSON = new Gson();
    private static final JsonParser parser = new JsonParser();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ByteBuf) {
            String text = ((ByteBuf) msg).toString(StandardCharsets.UTF_8);
            JsonElement jsonElement = parser.parse(text);
            String content = jsonElement.getAsJsonObject().get("content").getAsString();
            ByteBuf response = Unpooled.buffer(0);
            String json = GSON.toJson(outputJson(BracketHandlerCaller.call(content)));
//            ProbeZS.logger.info("Send " + json);
            response.writeCharSequence(json, StandardCharsets.UTF_8);
            ctx.channel().writeAndFlush(response);
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
