package youyihj.probezs.network;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.bracket.BracketHandlerCaller;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author youyihj
 */
public class BracketHandlerHttpHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final Pattern uriRegex = Pattern.compile("/([a-z]*)\\?q=(.*)");

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            Matcher matcher = uriRegex.matcher(request.uri());
            if (matcher.matches()) {
                String category = matcher.group(1);
                String query = matcher.group(2);
                String result;
                switch (category) {
                    case "icon":
                        result = BracketHandlerCaller.INSTANCE.getIcon(query);
                        break;
                    case "type":
                        result = BracketHandlerCaller.INSTANCE.getTypeName(query);
                        break;
                    case "name":
                        result = BracketHandlerCaller.INSTANCE.getLocalizedName(query);
                        break;
                    default:
                        notFound(ctx, request);
                        return;
                }
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.copiedBuffer(String.valueOf(result), StandardCharsets.UTF_8)
                );
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.writeAndFlush(response).addListener(future -> {
                    if (!HttpHeaderValues.KEEP_ALIVE.toString().equals(request.headers().get(HttpHeaderNames.CONNECTION))) {
                        ctx.close();
                    }
                });
            } else {
                notFound(ctx, request);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ProbeZS.logger.error(cause);
        ctx.close();
    }

    private void notFound(ChannelHandlerContext ctx, HttpRequest req) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        ctx.writeAndFlush(response).addListener(future -> {
            if (!HttpHeaderValues.KEEP_ALIVE.toString().equals(req.headers().get(HttpHeaderNames.CONNECTION))) {
                ctx.close();
            }
        });
    }
}
