package youyihj.probezs.tree;

import stanhebben.zenscript.annotations.Optional;
import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.member.ParameterData;
import youyihj.probezs.util.IndentStringBuilder;
import youyihj.probezs.util.ZenKeywords;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class ZenParameterNode implements IZenDumpable, ITypeNameContextAcceptor {
    private final Supplier<String> name;
    private final Supplier<JavaTypeMirror.Result> type;
    private final Optional optional;
    private final boolean varArgs;

    public ZenParameterNode(Supplier<String> name, Supplier<JavaTypeMirror.Result> type, Optional optional, boolean varArgs) {
        this.name = name;
        this.type = type;
        this.optional = optional;
        this.varArgs = varArgs;
    }

    public static ZenParameterNode read(ExecutableData method, int index, ParameterData parameter, ZenClassTree tree) {
        boolean varArgs = parameter.isVarargs();
        JavaTypeMirror returnType;
        if (varArgs && parameter.getType().isArray()) {
            returnType = tree.createJavaTypeMirror(parameter.getType().getComponentType());
        } else {
            returnType = tree.createJavaTypeMirror(parameter.getGenericType());
        }
        Supplier<String> name = () -> {
            List<String> list = tree.getMappings().find(method);
            if (list != null && index < list.size()) {
                return list.get(index);
            }
            return parameter.getName();
        };
        return new ZenParameterNode(name, returnType, parameter.getAnnotation(Optional.class), varArgs);
    }

    public static List<ZenParameterNode> read(ExecutableData method, int startIndex, ZenClassTree tree) {
        ParameterData[] parameters = method.getParameters();
        List<ZenParameterNode> parameterNodes = new ArrayList<>(method.getParameterCount());
        for (int i = startIndex; i < method.getParameterCount(); i++) {
            parameterNodes.add(ZenParameterNode.read(method, i, parameters[i], tree));
        }
        return parameterNodes;
    }

    public String getName() {
        String name = this.name.get();
        if (ZenKeywords.is(name)) {
            name = "_" + name;
        }
        if (varArgs) {
            name = "..." + name;
        }
        return name;
    }

    public JavaTypeMirror.Result getType() {
        return type.get();
    }

    public boolean isExisted() {
        if (type instanceof JavaTypeMirror) {
            return ((JavaTypeMirror) type).isExisted();
        } else {
            return true;
        }
    }

    public Optional getOptional() {
        return optional;
    }

    @Override
    public void toZenScript(IndentStringBuilder sb, TypeNameContext context) {
        String typeName = context.getTypeName(getType());
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
                        sb.append("default");
                    }
                    break;
            }
        }
    }

    @Override
    public void setMentionedTypes(TypeNameContext context) {
        context.addClasses(getType().getTypeVariables());
    }
}
