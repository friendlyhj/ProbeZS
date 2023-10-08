package youyihj.probezs.core.hook;

import stanhebben.zenscript.definitions.zenclasses.ParsedZenClass;

public interface ILocalVariableMethodOutput {

    void probeZS$enableLocalVariable();

    void probeZS$injectThis(ParsedZenClass zenClass);

    void probeZS$beginScope();

    void probeZS$endScope();

    void probeZS$addVariable(LocalVariable variable);


}
