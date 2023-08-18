package youyihj.probezs.tree;

import com.google.gson.*;
import stanhebben.zenscript.annotations.Optional;
import youyihj.probezs.docs.ParameterNameMappings;
import youyihj.probezs.util.IndentStringBuilder;
import youyihj.probezs.util.ZenKeywords;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class ZenParameterNode implements IZenDumpable, IHasImportMembers {
    private final Supplier<String> name;
    private final LazyZenClassNode type;
    private final Optional optional;
    private final boolean varArgs;

    public ZenParameterNode(Supplier<String> name, LazyZenClassNode type, Optional optional, boolean varArgs) {
        this.name = name;
        this.type = type;
        this.optional = optional;
        this.varArgs = varArgs;
    }

    public static ZenParameterNode read(Executable method, int index, Parameter parameter, ZenClassTree tree) {
        boolean varArgs = parameter.isVarArgs();
        LazyZenClassNode returnType;
        if (varArgs && parameter.getType().isArray()) {
            returnType = tree.createLazyClassNode(parameter.getType().getComponentType());
        } else {
            returnType = tree.createLazyClassNode(parameter.getParameterizedType());
        }
        Supplier<String> name = () -> {
            List<String> list = ParameterNameMappings.find(method);
            if (list != null && index < list.size()) {
                return list.get(index);
            }
            return parameter.getName();
        };
        return new ZenParameterNode(name, returnType, parameter.getAnnotation(Optional.class), varArgs);
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

    public LazyZenClassNode getType() {
        return type;
    }

    public Optional getOptional() {
        return optional;
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        String typeName = type.get().getQualifiedName();
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

    @Override
    public void fillImportMembers(Set<ZenClassNode> members) {
        members.addAll(type.get().getTypeVariables());
    }

    public static class Serializer implements JsonSerializer<ZenParameterNode> {

        @Override
        public JsonElement serialize(ZenParameterNode src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("name", src.getName());
            json.add("type", context.serialize(src.getType()));
            Optional optional = src.getOptional();
            if (optional != null) {
                String typeName = src.getType().get().getQualifiedName();
                switch (typeName) {
                    case "int":
                    case "short":
                    case "long":
                    case "byte":
                        json.addProperty("optional", optional.valueLong());
                        break;
                    case "bool":
                        json.addProperty("optional", optional.valueBoolean());
                        break;
                    case "float":
                    case "double":
                        json.addProperty("optional", optional.valueDouble());
                        break;
                    case "string":
                        if (optional.value().isEmpty()) {
                            json.add("optional", JsonNull.INSTANCE);
                        } else {
                            json.addProperty("optional", optional.value());
                        }
                        break;
                    default:
                        if (optional.methodClass() == Optional.class) {
                            json.add("optional", JsonNull.INSTANCE);
                        } else {
                            String sb = optional.methodClass().getName() +
                                    "." +
                                    optional.methodName() +
                                    "(" +
                                    "\"" + optional.value() + "\"" +
                                    ")";
                            json.addProperty("optional", sb);
                        }
                        break;
                }
            }
            return json;
        }
    }
}
