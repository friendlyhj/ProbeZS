package youyihj.probezs.socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author youyihj
 */
public class RpcSocketInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast("rpc", new RpcBracketCheckSocketHandler());
    }
}
