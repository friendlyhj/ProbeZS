package youyihj.probezs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import youyihj.probezs.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author youyihj
 */
public class Environment {
    private static final Map<String, Object> ENV_MAP = new TreeMap<>();
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    public static void put(String key, String value) {
        ENV_MAP.put(key, value);
    }

    public static void put(String key, List<String> value) {
        ENV_MAP.put(key, value);
    }

    public static void output(Path path) {

        try {
            FileUtils.createFile(path, GSON.toJson(ENV_MAP, new TypeToken<Map<String, String>>() {}.getType()));
        } catch (IOException e) {
            ProbeZS.logger.error("Failed to dump env", e);
        }
    }
}
