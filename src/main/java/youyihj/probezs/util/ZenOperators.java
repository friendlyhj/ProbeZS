package youyihj.probezs.util;

import stanhebben.zenscript.annotations.OperatorType;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author youyihj
 */
public class ZenOperators {
    private static final Map<OperatorType, String> OPERATORS = new EnumMap<>(OperatorType.class);

    static {
        OPERATORS.put(OperatorType.ADD, "+");
        OPERATORS.put(OperatorType.SUB, "-");
        OPERATORS.put(OperatorType.MUL, "*");
        OPERATORS.put(OperatorType.DIV, "/");
        OPERATORS.put(OperatorType.MOD, "%");
        OPERATORS.put(OperatorType.CAT, "~");
        OPERATORS.put(OperatorType.OR, "|");
        OPERATORS.put(OperatorType.AND, "&");
        OPERATORS.put(OperatorType.XOR, "^");
        OPERATORS.put(OperatorType.NEG, "-");
        OPERATORS.put(OperatorType.NOT, "!");
        OPERATORS.put(OperatorType.INDEXGET, "[]");
        OPERATORS.put(OperatorType.INDEXSET, "[]=");
        OPERATORS.put(OperatorType.RANGE, "..");
        OPERATORS.put(OperatorType.CONTAINS, "has");
        OPERATORS.put(OperatorType.COMPARE, "compare");
        OPERATORS.put(OperatorType.MEMBERGETTER, ".");
        OPERATORS.put(OperatorType.MEMBERSETTER, ".=");
        OPERATORS.put(OperatorType.EQUALS, "==");
    }

    public static String getZenScriptFormat(OperatorType operatorType) {
        return OPERATORS.get(operatorType);
    }
}
