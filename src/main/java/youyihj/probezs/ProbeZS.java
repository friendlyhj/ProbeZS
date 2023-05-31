package youyihj.probezs;

import crafttweaker.zenscript.GlobalRegistry;
import crafttweaker.zenscript.IBracketHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import stanhebben.zenscript.util.Pair;
import youyihj.probezs.bracket.ZenBracketTree;
import youyihj.probezs.tree.ZenClassTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
@Mod(modid = ProbeZS.MODID, name = ProbeZS.NAME, version = ProbeZS.VERSION, dependencies = ProbeZS.DEPENDENCIES)
public class ProbeZS {
    public static final String MODID = "probezs";
    public static final String VERSION = "1.5.2";
    public static final String NAME = "ProbeZS";
    public static final String DEPENDENCIES = "required-after:crafttweaker;";

    public static String mappings = "";

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        new Thread(() -> {
            try {
                URL url = new URL("https://friendlyhj.github.io/probezs-mappings/method-parameter-names.yaml");
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);
                try(BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                    mappings = reader.lines().collect(Collectors.joining("\n"));
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        ZenClassTree root = ZenClassTree.getRoot();
        root.readGlobals(GlobalRegistry.getGlobals());
        ZenBracketTree bracketTree = new ZenBracketTree(root);
        for (Pair<Integer, IBracketHandler> entry : GlobalRegistry.getPrioritizedBracketHandlers()) {
            bracketTree.addHandler(entry.getValue());
        }
        root.fresh();
        root.output();
//        bracketTree.output();
    }
}
