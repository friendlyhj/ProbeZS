package youyihj.probezs.core;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import youyihj.probezs.util.DebugAPIAdapter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author youyihj
 */
public class CoreMod implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {"youyihj.probezs.core.ProbeZSClassTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        DebugAPIAdapter.init();
//        Environment.put("launchArgs", getLaunchArguments());
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }


    @SuppressWarnings("unchecked")
    private List<String> getLaunchArguments() {
        List<String> args = new ArrayList<>();
        Map<String, String> forgeLaunchArgs = (Map<String, String>) Launch.blackboard.get("forgeLaunchArgs");
        forgeLaunchArgs.forEach((key, value) -> {
//            if (!"--accessToken".equals(key)) {
            args.add(key);
            args.add(value);
//            }
        });
        return args;
    }
}
