package youyihj.probezs.core;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import youyihj.probezs.Environment;
import youyihj.probezs.ProbeZSConfig;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        readMemberCollectorConfig((File) data.get("mcLocation"));
        Environment.put("launchArgs", getLaunchArguments());
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    private void readMemberCollectorConfig(File mcLocation) {
        Path configPath = Paths.get(mcLocation.toURI()).resolve("config/probezs.cfg");
        try {
            for (String line : Files.readAllLines(configPath, StandardCharsets.UTF_8)) {
                String trimmedLine = line.trim();
                String configKey = "S:memberCollector=";
                if (trimmedLine.startsWith(configKey)) {
                    ProbeZSConfig.memberCollector = ProbeZSConfig.MemberCollector.valueOf(trimmedLine.substring(configKey.length()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getLaunchArguments() {
        List<String> args = new ArrayList<>();
        Map<String, String> forgeLaunchArgs = (Map<String, String>) Launch.blackboard.get("forgeLaunchArgs");
        forgeLaunchArgs.forEach((key, value) -> {
//            if (!"--accessToken".equals(key)) {
                args.add(key + " " + value);
//            }
        });
        return args;
    }
}
