package youyihj.probezs.socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;

/**
 * @author youyihj
 */
public class SocketInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(8192));
        pipeline.addLast("websocketCompression", new WebSocketServerCompressionHandler());
        pipeline.addLast("websocketProtocol", new WebSocketServerProtocolHandler("/probezs", null,true));
        pipeline.addLast("bracketHandler", new BracketCheckHandler());
    }
}
