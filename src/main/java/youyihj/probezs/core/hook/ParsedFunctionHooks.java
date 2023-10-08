package youyihj.probezs.core.hook;

import stanhebben.zenscript.definitions.ParsedFunction;

public class ParsedFunctionHooks {

    public static ThreadLocal<Boolean> isGeneratingStatement = ThreadLocal.withInitial(() -> false);
    public static ThreadLocal<ParsedFunction> currentFunction = new ThreadLocal<>();
}
