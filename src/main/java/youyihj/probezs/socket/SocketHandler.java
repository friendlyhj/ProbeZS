package youyihj.probezs.socket;

import crafttweaker.CraftTweakerAPI;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import youyihj.probezs.ProbeZSConfig;

/**
 * @author youyihj
 */
public class SocketHandler {
    public static SocketHandler INSTANCE = null;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    public SocketHandler() {
        new Thread(this::handleServerSocket, "ProbeZS-Server-Websocket").start();
    }

    public static void enable() {
        if (INSTANCE == null) {
            INSTANCE = new SocketHandler();
        }
    }

    private void handleServerSocket() {
        try {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            ChannelHandler childHandler = null;
            switch (ProbeZSConfig.socketProtocol) {
                case WEBSOCKET:
                    childHandler = new SocketInitializer();
                    break;
                case JSONRPC:
                    childHandler = new RpcSocketInitializer();
                    break;
            }

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(childHandler)
                    .option(ChannelOption.SO_BACKLOG, 512)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            Channel ch = b.bind(ProbeZSConfig.socketPort).sync().channel();

            ch.closeFuture().sync();
        } catch(InterruptedException e) {
            CraftTweakerAPI.logError("Error while in Socket Thread", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
