package youyihj.probezs.bracket;

import crafttweaker.api.item.IIngredient;
import youyihj.probezs.tree.ZenClassTree;

/**
 * @author youyihj
 */
public class IngredientAnyBracketNode extends ZenBracketNode {
    public IngredientAnyBracketNode(ZenClassTree tree) {
        super(tree.createLazyClassNode(IIngredient.class), 0);
        addContent("*");
    }

    @Override
    public String getName() {
        return "ingredientAny";
    }
}
