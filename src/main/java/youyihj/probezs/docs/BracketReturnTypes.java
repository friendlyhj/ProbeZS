package youyihj.probezs.docs;

import crafttweaker.zenscript.IBracketHandler;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author youyihj
 */
public class BracketReturnTypes {
    private static Map<String, String> values;

    public static void load(String path) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = BracketReturnTypes.class.getClassLoader().getResourceAsStream(path)) {
            values = yaml.loadAs(inputStream, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> find(IBracketHandler bracketHandler) {
        if (values == null) {
            load("mappings/bracket-return-types.yaml");
        }
        String type = values.get(bracketHandler.getClass().getCanonicalName());
        if (type == null) {
            return bracketHandler.getReturnedClass();
        } else {
            try {
                return Class.forName(type);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }
}
