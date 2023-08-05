package youyihj.probezs.socket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
        JsonObject request = ((JsonObject) parser.parse(msg.toString(StandardCharsets.UTF_8)));
        if (request.get("jsonrpc").getAsString().equals("2.0")) {
            int id = request.get("id").getAsInt();
            String method = request.get("method").getAsString();
            if (method.equals("call")) {
                JsonArray params = request.get("params").getAsJsonArray();
                BracketHandlerResult result = BracketHandlerCaller.INSTANCE.call(params.get(0).getAsString(), params.get(1).getAsBoolean());
                JsonObject response = new JsonObject();
                response.addProperty("jsonrpc", "2.0");
                response.add("result", BracketCheckHandler.outputJson(result));
                response.addProperty("id", id);
                ByteBuf buffer = Unpooled.buffer(0);
                buffer.writeCharSequence(response.toString(), StandardCharsets.UTF_8);
                ctx.channel().writeAndFlush(buffer);
            }
        }
    }
}
