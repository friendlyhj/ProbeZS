package youyihj.probezs;

import net.minecraftforge.common.config.Config;

/**
 * @author youyihj
 */
@Config(modid = ProbeZS.MODID)
public class ProbeZSConfig {

    @Config.Comment("whether output dzs files")
    public static boolean dumpDZS = true;

    @Config.Comment("If true, outputs the source of expansion members in dzs")
    public static boolean outputSourceExpansionMembers = false;

    @Config.Comment("Dump Native members")
    public static boolean dumpNativeMembers = false;
}
