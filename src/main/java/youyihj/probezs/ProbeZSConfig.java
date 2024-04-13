package youyihj.probezs;

import net.minecraftforge.common.config.Config;

/**
 * @author youyihj
 */
@Config(modid = ProbeZS.MODID)
public class ProbeZSConfig {

    @Config.Comment("whether output dzs files")
    public static boolean dumpDZS = true;

    @Config.Comment("whether output json files")
    public static boolean dumpJson = false;

    @Config.Comment("Communication port with language server")
    public static int socketPort = 6489;

    @Config.Comment("The way to collect zenscript libs")
    public static MemberCollector memberCollector = MemberCollector.REFLECTION;

    @Config.Comment("If true, outputs the source of expansion members in dzs")
    public static boolean outputSourceExpansionMembers = false;

    public enum MemberCollector {
        REFLECTION,
        ASM;
    }
}
