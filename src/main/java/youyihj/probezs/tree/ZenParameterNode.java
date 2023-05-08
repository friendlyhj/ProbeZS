package youyihj.probezs.tree;

import stanhebben.zenscript.annotations.Optional;
import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Parameter;

/**
 * @author youyihj
 */
public class ZenParameterNode {
    private final String name;
    private final LazyZenClassNode type;
    private final Optional optional;

    public ZenParameterNode(String name, LazyZenClassNode type, Optional optional) {
        this.name = name;
        this.type = type;
        this.optional = optional;
    }

    public static ZenParameterNode read(Parameter parameter, ZenClassTree tree) {
        return new ZenParameterNode(parameter.getName(), tree.createLazyClassNode(parameter.getParameterizedType()), parameter.getAnnotation(Optional.class));
    }

    public void toZenScript(IndentStringBuilder sb) {
        String typeName = type.isExisted() ? type.get().getName() : "unknown";
        sb.append(name).append(" as ").append(typeName);
        if (optional != null) {
            sb.append(" = ");
            switch (typeName) {
                case "int":
                case "short":
                case "long":
                case "byte":
                    sb.append(String.valueOf(optional.valueLong()));
                    break;
                case "bool":
                    sb.append(String.valueOf(optional.valueBoolean()));
                    break;
                case "float":
                case "double":
                    sb.append(String.valueOf(optional.valueDouble()));
                    break;
                case "string":
                    sb.append("\"").append(optional.value()).append("\"");
                    break;
                default:
                    sb.append(optional.methodClass().getName())
                            .append(".")
                            .append(optional.methodName())
                            .append("(")
                            .append("\"").append(optional.value()).append("\"")
                            .append(")");
                    break;
            }
        }
    }
}
