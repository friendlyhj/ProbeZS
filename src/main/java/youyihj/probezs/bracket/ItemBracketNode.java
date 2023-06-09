package youyihj.probezs.bracket;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IItemStack;
import youyihj.probezs.tree.ZenClassTree;
import youyihj.probezs.util.IndentStringBuilder;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author youyihj
 */
public class ItemBracketNode extends ZenBracketNode {
    private final Map<IItemStack, String> items = new LinkedHashMap<>();

    public ItemBracketNode(ZenClassTree tree) {
        super(tree.createLazyClassNode(IItemStack.class), 0);
        readItems();
    }

    private void readItems() {
        CraftTweakerAPI.game.getItems().stream()
                .flatMap(it -> it.getSubItems().stream())
                .filter(it -> !it.hasTag())
                .forEach(it -> items.put(it, it.getDisplayName()));
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        sb.append("val items as string[crafttweaker.item.IItemStack] = {");
        sb.push();
        Iterator<Map.Entry<IItemStack, String>> iterator = items.entrySet().iterator();
        int lineElement = 0;
        Map.Entry<IItemStack, String> first = iterator.next();
        sb.append(first.getKey().toCommandString()).append(" : \"").append(first.getValue()).append("\"");
        lineElement++;
        while (iterator.hasNext()) {
            sb.append(",");
            if (lineElement == ELEMENTS_ONE_LINE) {
                sb.nextLine();
                lineElement = 0;
            } else {
                sb.append(" ");
            }
            Map.Entry<IItemStack, String> next = iterator.next();
            sb.append(next.getKey().toCommandString()).append(" : \"").append(next.getValue()).append("\"");
            lineElement++;
        }
        sb.pop();
        sb.append("};");
    }

    @Override
    public String getName() {
        return "items";
    }
}
