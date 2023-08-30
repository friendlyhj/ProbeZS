package youyihj.probezs.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import youyihj.probezs.ProbeZSConfig;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        File mcLocation = (File) data.get("mcLocation");
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

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
