package youyihj.probezs.bracket;

import crafttweaker.api.item.IIngredient;
import youyihj.probezs.tree.ZenClassTree;

/**
 * @author youyihj
 */
public class IngredientAnyBracketNode extends ZenBracketNode {
    public IngredientAnyBracketNode(ZenClassTree tree) {
        super(tree.createLazyClassNode(IIngredient.class));
        addContent("*");
    }
}
