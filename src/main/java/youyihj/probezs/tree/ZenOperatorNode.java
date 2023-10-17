package youyihj.probezs.tree;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import youyihj.probezs.util.IntersectionType;
import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class ZenOperatorNode extends ZenExecutableNode implements IZenDumpable, IHasImportMembers {
    private final String name;
    private final List<ZenParameterNode> parameters;

    protected Supplier<JavaTypeMirror.Result> returnType;

    public ZenOperatorNode(String name, List<ZenParameterNode> parameters, Supplier<JavaTypeMirror.Result> returnTypes) {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnTypes;
    }

    @Override
    public void fillImportMembers(Set<ZenClassNode> members) {
        for (ZenParameterNode parameter : parameters) {
            parameter.fillImportMembers(members);
        }
        members.addAll(returnType.get().getTypeVariables());
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        if (parameters.stream().map(ZenParameterNode::getType).allMatch(JavaTypeMirror::isExisted)) {
            sb.append("operator ");
            partialDump(sb, name, parameters, returnType.get());
        }
    }

    public static class As extends ZenOperatorNode {

        private IntersectionType intersectionType;
        private final ZenClassTree tree;

        public As(ZenClassTree tree) {
            super("as", Collections.emptyList(), null);
            this.tree = tree;
        }

        public void appendCastType(Type type) {
            if (intersectionType == null) {
                intersectionType = new IntersectionType(new Type[] {type});
            } else {
                intersectionType = intersectionType.append(type);
            }
            returnType = tree.createJavaTypeMirror(intersectionType);
        }
    }

    public static class AsSerializer implements JsonSerializer<As> {

        @Override
        public JsonElement serialize(As src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src, ZenOperatorNode.class);
        }
    }
}
