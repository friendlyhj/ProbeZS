package youyihj.probezs.tree;

import com.google.gson.annotations.SerializedName;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenMethodStatic;
import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class ZenMemberNode extends ZenExecutableNode implements IZenDumpable, IHasImportMembers, Comparable<ZenMemberNode> {
    private final String name;
    private final List<ZenParameterNode> parameters;
    private final boolean isStatic;

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
        return parameters.stream().map(ZenParameterNode::getType).allMatch(JavaTypeMirror::isExisted);
    }

    @Override
    protected void writeModifiersAndName(IndentStringBuilder sb) {
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
    public void fillImportMembers(Set<ZenClassNode> members) {
        for (ZenParameterNode parameter : parameters) {
            parameter.fillImportMembers(members);
        }
        members.addAll(returnTypeResultSupplier.get().getTypeVariables());
    }

    @Override
    public int compareTo(ZenMemberNode o) {
        if (this.isStatic != o.isStatic) {
            return this.isStatic ? -1 : 1; // static members come first
        }
        int nameComparison = this.name.compareTo(o.name);
        if (nameComparison != 0) {
            return nameComparison;
        }
        int thisParamSize = this.parameters.size();
        int otherParamSize = o.parameters.size();
        if (thisParamSize != otherParamSize) {
            return Integer.compare(thisParamSize, otherParamSize);
        }
        for (int i = 0; i < thisParamSize; i++) {
            int paramComparison = this.parameters.get(i).getType().compareTo(o.parameters.get(i).getType());
            if (paramComparison != 0) {
                return paramComparison;
            }
        }
        return this.returnTypeResultSupplier.get().getFullName().compareTo(o.returnTypeResultSupplier.get().getFullName());
    }
}
