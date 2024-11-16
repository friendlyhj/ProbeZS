package youyihj.probezs.tree;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import youyihj.probezs.util.CastRuleType;
import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class ZenOperatorNode extends ZenExecutableNode implements IZenDumpable, ITypeNameContextAcceptor {
    private final String name;
    private final List<ZenParameterNode> parameters;

    protected Supplier<JavaTypeMirror.Result> returnType;

    public ZenOperatorNode(String name, List<ZenParameterNode> parameters, Supplier<JavaTypeMirror.Result> returnTypes) {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnTypes;
    }

    @Override
    protected boolean existed() {
        return parameters.stream().allMatch(ZenParameterNode::isExisted);
    }

    @Override
    protected void writeModifiersAndName(IndentStringBuilder sb) {
        sb.append("operator ").append(name);
    }

    @Override
    protected List<ZenParameterNode> getParameters() {
        return parameters;
    }

    @Override
    protected JavaTypeMirror.Result getReturnType() {
        return returnType.get();
    }

    @Override
    public void setMentionedTypes(TypeNameContext context) {
        for (ZenParameterNode parameter : parameters) {
            parameter.setMentionedTypes(context);
        }
        context.addClasses(returnType.get().getTypeVariables());
    }

    public static class As extends ZenOperatorNode {

        private final CastRuleType castRuleType = new CastRuleType();

        public As(ZenClassTree tree) {
            super("as", Collections.emptyList(), null);
            this.returnType = tree.createJavaTypeMirror(castRuleType);
        }

        public void appendCastType(Type type) {
            castRuleType.appendType(type);
        }
    }

    public static class AsSerializer implements JsonSerializer<As> {

        @Override
        public JsonElement serialize(As src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src, ZenOperatorNode.class);
        }
    }
}
