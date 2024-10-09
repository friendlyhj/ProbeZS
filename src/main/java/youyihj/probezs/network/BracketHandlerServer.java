package youyihj.probezs.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import youyihj.probezs.ProbeZS;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author youyihj
 */
public class BracketHandlerServer {
    public static void start() {
        new Thread(() -> {
            try {
                BracketHandlerServer.runServer();
            } catch (Exception e) {
                ProbeZS.logger.error(e);
            }
        }, "ProbeZS Bracket Handler Http Server").start();
    }

    private static void runServer() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new BracketHandlerHttpHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            int port = getPort();
            if (port == 0) return;
            ChannelFuture f = b.bind(port).sync();
            ProbeZS.logger.info("Bracket Handler HTTP Server started on port {}", port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private static int getPort() {
        Path path = FileSystems.getDefault().getPath("intellizen.json");
        if (!Files.exists(path)) {
            path = FileSystems.getDefault().getPath("scripts", "probezs.json");
        }
        try {
            JsonObject json = new Gson().fromJson(Files.newBufferedReader(path), JsonElement.class).getAsJsonObject();
            return json.getAsJsonObject("probezs")
                    .get("port")
                    .getAsInt();
        } catch (Exception ignored) {
        }
        return 0;
    }
}
