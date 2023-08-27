package youyihj.probezs;

import com.cleanroommc.configanytime.ConfigAnytime;
import net.minecraftforge.common.config.Config;

/**
 * @author youyihj
 */
@Config(modid = ProbeZS.MODID)
public class ProbeZSConfig {
    public static boolean dumpDZS = true;
    public static boolean dumpJson = false;
    public static int socketPort = 6489;
    public static SocketProtocol socketProtocol = SocketProtocol.JSONRPC;
    public static MemberCollector memberCollector = MemberCollector.REFLECTION;

    public enum SocketProtocol {
        NONE,
        WEBSOCKET,
        JSONRPC
    }

    public enum MemberCollector {
        REFLECTION,
        ASM;
    }

    static {
        ConfigAnytime.register(ProbeZSConfig.class);
    }
}
