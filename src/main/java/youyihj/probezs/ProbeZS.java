package youyihj.probezs;

import crafttweaker.zenscript.GlobalRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import youyihj.probezs.tree.ZenClassTree;

/**
 * @author youyihj
 */
@Mod(modid = ProbeZS.MODID, name = ProbeZS.NAME, version = ProbeZS.VERSION, dependencies = ProbeZS.DEPENDENCIES)
public class ProbeZS {
    public static final String MODID = "probezs";
    public static final String VERSION = "1.2";
    public static final String NAME = "ProbeZS";
    public static final String DEPENDENCIES = "required-after:crafttweaker;";

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        ZenClassTree root = ZenClassTree.getRoot();
        root.readGlobals(GlobalRegistry.getGlobals());
        root.fresh();
        root.output();
    }
}
