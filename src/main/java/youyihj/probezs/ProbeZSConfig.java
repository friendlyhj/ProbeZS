package youyihj.probezs;

import net.minecraftforge.common.config.Config;

/**
 * @author youyihj
 */
@Config(modid = ProbeZS.MODID)
public class ProbeZSConfig {
    public static boolean dumpDZS = true;
    public static boolean dumpJson = false;
    public static int socketPort = 6489;
    public static SocketProtocol socketProtocol = SocketProtocol.RPC;

    public enum SocketProtocol {
        NONE,
        WEBSOCKET,
        RPC
    }
}
