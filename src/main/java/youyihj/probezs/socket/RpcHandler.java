package youyihj.probezs.socket;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.ProbeZSConfig;
import youyihj.probezs.bracket.BracketHandlerCaller;
import youyihj.probezs.api.IBracketHandlerCaller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author youyihj
 */
public class RpcHandler {
    private static RpcHandler INSTANCE;

    public RpcHandler() {
        Thread thread = new Thread(this::handleRpc, "ProbeZS-Server-Rpc");
        thread.setDaemon(true);
        thread.start();
    }

    public static void enable() {
        if (INSTANCE == null) {
            INSTANCE = new RpcHandler();
        }
    }

    public void handleRpc() {
        JsonRpcServer rpcServer = new JsonRpcServer(new SerializableBracketHandlerCaller(BracketHandlerCaller.INSTANCE), IBracketHandlerCaller.class);
        try (ServerSocket serverSocket = new ServerSocket(ProbeZSConfig.socketPort)) {
            Socket socket = serverSocket.accept();
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    rpcServer.handleRequest(socket.getInputStream(), socket.getOutputStream());
                } catch (SocketException e) {
                    ProbeZS.logger.error(e);
                    socket = serverSocket.accept();
                }
            }
        } catch (IOException e) {
            ProbeZS.logger.error(e);
        }
    }
}
