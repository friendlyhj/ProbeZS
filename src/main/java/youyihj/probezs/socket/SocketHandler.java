package youyihj.probezs.socket;

import crafttweaker.CraftTweakerAPI;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import youyihj.probezs.ProbeZSConfig;

/**
 * @author youyihj
 */
public class SocketHandler {
    public static SocketHandler INSTANCE = null;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

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
            bossGroup = new OioEventLoopGroup(1);
            workerGroup = new OioEventLoopGroup();

            ChannelHandler childHandler = null;
            switch (ProbeZSConfig.socketProtocol) {
                case WEBSOCKET:
                    childHandler = new SocketInitializer();
                    break;
                case RPC:
                    childHandler = new RpcSocketInitializer();
                    break;
            }

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(OioServerSocketChannel.class)
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
