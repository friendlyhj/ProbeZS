package youyihj.probezs.tree;

import stanhebben.zenscript.annotations.Optional;
import youyihj.probezs.docs.ParameterNameMappings;
import youyihj.probezs.util.IndentStringBuilder;
import youyihj.probezs.util.ZenKeywords;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class ZenParameterNode implements IZenDumpable {
    private final Supplier<String> name;
    private final LazyZenClassNode type;
    private final Optional optional;

    public ZenParameterNode(Supplier<String> name, LazyZenClassNode type, Optional optional) {
        this.name = name;
        this.type = type;
        this.optional = optional;
    }

    public static ZenParameterNode read(Method method, int index, Parameter parameter, ZenClassTree tree) {
        Supplier<String> name = () -> {
            List<String> list = ParameterNameMappings.find(method);
            if (list != null && index < list.size()) {
                return list.get(index);
            }
            return parameter.getName();
        };
        return new ZenParameterNode(name, tree.createLazyClassNode(parameter.getParameterizedType()), parameter.getAnnotation(Optional.class));
    }

    public String getName() {
        String name = this.name.get();
        if (ZenKeywords.is(name)) {
            return "_" + name;
        }
        return name;
    }

    public LazyZenClassNode getType() {
        return type;
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        String typeName = type.isExisted() ? type.get().getName() : "unknown";
        sb.append(getName()).append(" as ").append(typeName);
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
                    if (optional.value().isEmpty()) {
                        sb.append(null);
                    } else {
                        sb.append("\"").append(optional.value()).append("\"");
                    }
                    break;
                default:
                    if (optional.methodClass() == Optional.class) {
                        sb.append(null);
                    } else {
                        sb.append(optional.methodClass().getName())
                                .append(".")
                                .append(optional.methodName())
                                .append("(")
                                .append("\"").append(optional.value()).append("\"")
                                .append(")");
                    }
                    break;
            }
        }
    }
}
