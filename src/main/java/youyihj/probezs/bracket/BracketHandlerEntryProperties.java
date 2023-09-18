package youyihj.probezs.bracket;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author youyihj
 */
public class BracketHandlerEntryProperties {
    private final Map<String, JsonElement> properties = new HashMap<>();

    public void add(String key, JsonElement value, boolean meta) {
        if (meta) {
            key = "_" + key;
        }
        properties.put(key, value);
    }

    public void add(String key, String value, boolean meta) {
        add(key, new JsonPrimitive(value), meta);
    }

    public void add(String key, List<String> value, boolean meta) {
        JsonArray array = new JsonArray();
        value.forEach(it -> array.add(new JsonPrimitive(it)));
        add(key, array, meta);
    }

    public Map<String, JsonElement> getProperties() {
        return properties;
    }

    public static class Serializer implements JsonSerializer<BracketHandlerEntryProperties> {

        @Override
        public JsonElement serialize(BracketHandlerEntryProperties src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.getProperties(), new TypeToken<Map<String, JsonElement>>() {}.getType());
        }
    }
}
