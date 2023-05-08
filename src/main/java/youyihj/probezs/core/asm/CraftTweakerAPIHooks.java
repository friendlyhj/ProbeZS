package youyihj.probezs.core.asm;

import youyihj.probezs.tree.ZenClassTree;

/**
 * @author youyihj
 */
public class CraftTweakerAPIHooks {
    public static void readClass(Class<?> clazz) {
        ZenClassTree.getRoot().putClass(clazz);
    }
}
