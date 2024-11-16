package youyihj.probezs.tree;

import com.google.gson.annotations.SerializedName;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenMethodStatic;
import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class ZenMemberNode extends ZenExecutableNode implements IZenDumpable, ITypeNameContextAcceptor {
    private final String name;
    private final List<ZenParameterNode> parameters;
    private final boolean isStatic;
    private boolean isLambda;

    @SerializedName("returnType")
    private final Supplier<JavaTypeMirror.Result> returnTypeResultSupplier;

    public ZenMemberNode(String name, Supplier<JavaTypeMirror.Result> returnType, List<ZenParameterNode> parameters, boolean isStatic) {
        this.name = name;
        this.parameters = parameters;
        this.isStatic = isStatic;
        this.returnTypeResultSupplier = returnType;
    }

    public static ZenMemberNode read(ExecutableData method, ZenClassTree tree, boolean isClass) {
        if (isClass && method.isAnnotationPresent(ZenMethod.class)) {
            String name = method.getAnnotation(ZenMethod.class).value();
            if (name.isEmpty()) {
                name = method.getName();
            }
            return readDirectly(method, tree, name, Modifier.isStatic(method.getModifiers()), false);
        }
        if (!isClass) {
            if (method.isAnnotationPresent(ZenMethod.class)) {
                String name = method.getAnnotation(ZenMethod.class).value();
                if (name.isEmpty()) {
                    name = method.getName();
                }
                return readDirectly(method, tree, name, false, true);
            }
            if (method.isAnnotationPresent(ZenMethodStatic.class)) {
                String name = method.getAnnotation(ZenMethodStatic.class).value();
                if (name.isEmpty()) {
                    name = method.getName();
                }
                return readDirectly(method, tree, name, true, false);
            }
        }
        return null;
    }

    public static ZenMemberNode readDirectly(ExecutableData method, ZenClassTree tree, String name, boolean isStatic, boolean expansion) {
        int startIndex = expansion ? 1 : 0;
        return new ZenMemberNode(
                name,
                tree.createJavaTypeMirror(method.getReturnType()),
                ZenParameterNode.read(method, startIndex, tree),
                isStatic
        );
    }

    @Override
    protected boolean existed() {
        return parameters.stream().allMatch(ZenParameterNode::isExisted);
    }

    @Override
    protected void writeModifiersAndName(IndentStringBuilder sb) {
        if (isLambda) {
            sb.append("lambda ");
        }
        if (isStatic) {
            sb.append("static ");
        }
        sb.append("function ").append(name);
    }

    @Override
    protected List<ZenParameterNode> getParameters() {
        return parameters;
    }

    @Override
    protected JavaTypeMirror.Result getReturnType() {
        return returnTypeResultSupplier.get();
    }

    @Override
    public void setMentionedTypes(TypeNameContext context) {
        for (ZenParameterNode parameter : parameters) {
            parameter.setMentionedTypes(context);
        }
        context.addClasses(returnTypeResultSupplier.get().getTypeVariables());
    }

    public void setLambda() {
        isLambda = true;
    }
}
