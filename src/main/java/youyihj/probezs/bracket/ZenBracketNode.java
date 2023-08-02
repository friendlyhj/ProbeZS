package youyihj.probezs.bracket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import youyihj.probezs.tree.LazyZenClassNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author youyihj
 */
public class ZenBracketNode {
    private final List<String> content = new ArrayList<>();
    private final LazyZenClassNode type;

    public ZenBracketNode(LazyZenClassNode type) {
        this.type = type;
    }

    public void addContent(List<String> content) {
        this.content.addAll(content);
    }

    public void addContent(String content) {
        this.content.add(content);
    }

    public final JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type.get().getTypeVariables().get(0).getName());
        JsonArray contentsJson = new JsonArray();
        fillJsonContents(contentsJson);
        json.add("contents", contentsJson);
        return json;
    }

    public void fillJsonContents(JsonArray jsonArray) {
        for (String s : this.content) {
            jsonArray.add(s);
        }
    }
}
