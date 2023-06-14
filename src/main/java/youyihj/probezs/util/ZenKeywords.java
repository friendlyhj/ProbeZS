package youyihj.probezs.util;

import crafttweaker.mc1120.util.CraftTweakerHacks;
import stanhebben.zenscript.ZenTokener;

import java.util.HashMap;
import java.util.Set;

/**
 * @author youyihj
 */
public class ZenKeywords {
    private static final Set<String> KEYWORDS;

    static {
        HashMap<String, Integer> tokenKeywords = CraftTweakerHacks.getPrivateStaticObject(ZenTokener.class, "KEYWORDS");
        KEYWORDS = tokenKeywords.keySet();
    }

    public static boolean is(String s) {
        return KEYWORDS.contains(s);
    }

    public static Set<String> get() {
        return KEYWORDS;
    }
}
