package youyihj.probezs.socket;

import com.google.gson.GsonBuilder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import youyihj.probezs.socket.rpc.RpcCodec;

import java.nio.charset.StandardCharsets;

/**
 * @author youyihj
 */
public class RpcSocketInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8), new StringEncoder(StandardCharsets.UTF_8));
        ch.pipeline().addLast(new RpcCodec(new GsonBuilder().disableHtmlEscaping().create()));
        ch.pipeline().addLast("rpc", new RpcBracketCheckSocketHandler());
    }
}
