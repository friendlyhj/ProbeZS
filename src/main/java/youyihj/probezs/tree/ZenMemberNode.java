package youyihj.probezs.tree;

import com.google.gson.annotations.SerializedName;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenMethodStatic;
import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class ZenMemberNode extends ZenExecutableNode implements IZenDumpable, IHasImportMembers {
    private final String name;
    private final List<ZenParameterNode> parameters;
    private final boolean isStatic;

    @SerializedName("returnType")
    private final Supplier<LazyZenClassNode.Result> returnTypeResultSupplier;

    public ZenMemberNode(String name, Supplier<LazyZenClassNode.Result> returnType, List<ZenParameterNode> parameters, boolean isStatic) {
        this.name = name;
        this.parameters = parameters;
        this.isStatic = isStatic;
        this.returnTypeResultSupplier = returnType;
    }

    public static ZenMemberNode read(Method method, ZenClassTree tree, boolean isClass) {
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

    public static ZenMemberNode readDirectly(Method method, ZenClassTree tree, String name, boolean isStatic, boolean expansion) {
        int startIndex = expansion ? 1 : 0;
        return new ZenMemberNode(
                name,
                tree.createLazyClassNode(method.getGenericReturnType()),
                ZenParameterNode.read(method, startIndex, tree),
                isStatic
        );
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        if (parameters.stream().map(ZenParameterNode::getType).allMatch(LazyZenClassNode::isExisted)) {
            if (isStatic) {
                sb.append("static ");
            }
            sb.append("function ");
            partialDump(sb, name, parameters, returnTypeResultSupplier.get());
        }
    }

    @Override
    public void fillImportMembers(Set<ZenClassNode> members) {
        for (ZenParameterNode parameter : parameters) {
            parameter.fillImportMembers(members);
        }
        members.addAll(returnTypeResultSupplier.get().getTypeVariables());
    }
}
