package youyihj.probezs.bracket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IItemStack;
import youyihj.probezs.tree.ZenClassTree;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author youyihj
 */
public class ItemBracketNode extends ZenBracketNode {
    private final Map<IItemStack, String> items = new LinkedHashMap<>();

    public ItemBracketNode(ZenClassTree tree) {
        super(tree.createLazyClassNode(IItemStack.class));
        readItems();
    }

    private void readItems() {
        CraftTweakerAPI.game.getItems().stream()
                .flatMap(it -> it.getSubItems().stream())
                .filter(it -> !it.hasTag())
                .forEach(it -> items.put(it, it.getDisplayName()));
    }

    @Override
    public void fillJsonContents(JsonArray jsonArray) {
        items.forEach((item, name) -> {
            JsonObject element = new JsonObject();
            String commandString = item.toCommandString();
            element.addProperty("id", commandString.substring(1, commandString.length() - 1));
            element.addProperty("name", item.getDisplayName());
            jsonArray.add(element);
        });
    }
}
