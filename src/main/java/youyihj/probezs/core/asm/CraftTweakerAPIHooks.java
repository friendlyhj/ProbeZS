package youyihj.probezs.core.asm;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.ProbeZSConfig;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author youyihj
 */
public class CraftTweakerAPIHooks {
    public static final Set<Class<?>> ZEN_CLASSES = new HashSet<>();


    public static void readClass(Class<?> clazz) {
        Map<String, ModContainer> pathToModMap = ProbeZS.pathToModMap;
        if (ProbeZSConfig.outputSourceExpansionMembers && pathToModMap.isEmpty()) {
            Loader.instance().getActiveModList().forEach(mod -> {
                String uri = mod.getSource().toURI().toString();
                if (!pathToModMap.containsKey(uri)) {
                    pathToModMap.put(uri, mod);
                }
            });
        }
        ZEN_CLASSES.add(clazz);
    }
}
