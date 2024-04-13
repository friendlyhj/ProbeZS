package youyihj.probezs.core.asm;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.ProbeZSConfig;
import youyihj.probezs.tree.ZenClassTree;

import java.util.Map;

/**
 * @author youyihj
 */
public class CraftTweakerAPIHooks {
    public static void readClass(Class<?> clazz) {
        Map<String, ModContainer> pathToModMap = ProbeZS.instance.pathToModMap;
        if (ProbeZSConfig.outputSourceExpansionMembers && pathToModMap.isEmpty()) {
            Loader.instance().getActiveModList().forEach(mod -> {
                String uri = mod.getSource().toURI().toString();
                if (!pathToModMap.containsKey(uri)) {
                    pathToModMap.put(uri, mod);
                }
            });
        }
        ZenClassTree.getRoot().putClass(clazz);
    }
}
