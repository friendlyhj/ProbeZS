package youyihj.probezs.util;

import stanhebben.zenscript.annotations.OperatorType;

/**
 * @author youyihj
 */
public enum ZenOperators {
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    MOD("%"),
    CAT("~"),
    OR("|"),
    AND("&"),
    XOR("^"),
    NEG("-"),
    NOT("!"),
    INDEXGET("[]"),
    INDEXSET("[]="),
    RANGE(".."),
    CONTAINS("has"),
    EQUALS("=="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    MEMBERGETTER("."),
    MEMBERSETTER(".="),
    FOR_IN("for_in"),
    AS("as");

    private final String symbol;

    ZenOperators(String symbol) {
        this.symbol = symbol;
    }

    public static ZenOperators getZenScriptFormat(OperatorType operatorType) {
        return valueOf(operatorType.name());
    }

    public String getSymbol() {
        return symbol;
    }
}
