package youyihj.probezs.bracket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * @author youyihj
 */
public class BracketHandlerEntry {
    private final String id;
    private final BracketHandlerEntryProperties properties;

    public BracketHandlerEntry(String id, BracketHandlerEntryProperties properties) {
        this.id = id;
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public BracketHandlerEntryProperties getProperties() {
        return properties;
    }

    public static class Serializer implements JsonSerializer<BracketHandlerEntry> {

        @Override
        public JsonElement serialize(BracketHandlerEntry src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("id", src.getId());
            src.getProperties().getProperties().forEach(json::add);
            return json;
        }
    }
}
