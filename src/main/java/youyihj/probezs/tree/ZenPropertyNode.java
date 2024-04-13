package youyihj.probezs.tree;

import youyihj.probezs.util.IndentStringBuilder;

import java.util.Set;

/**
 * @author youyihj
 */
public class ZenPropertyNode implements IZenDumpable, IHasImportMembers, IMaybeExpansionMember {
    private final JavaTypeMirror type;
    private final String name;

    private boolean hasGetter;
    private boolean hasSetter;

    private boolean isStatic;
    private String owner;

    public ZenPropertyNode(JavaTypeMirror type, String name) {
        this.type = type;
        this.name = name;
    }

    public boolean isHasGetter() {
        return hasGetter;
    }

    public void setHasGetter(boolean hasGetter) {
        this.hasGetter = hasGetter;
    }

    public boolean isHasSetter() {
        return hasSetter;
    }

    public void setHasSetter(boolean hasSetter) {
        this.hasSetter = hasSetter;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        if (type.isExisted()) {
            if (owner != null) {
                sb.append("// expansion member from ").append(owner).nextLine();
            }
            String declareKeyword = isStatic ? "static" : isHasSetter() ? "var" : "val";
            sb.append(declareKeyword);
            sb.append(" ").append(name).append(" as ").append(type.get().getQualifiedName()).append(";");
        }
    }

    @Override
    public void fillImportMembers(Set<ZenClassNode> members) {
        members.addAll(type.get().getTypeVariables());
    }


    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getOwner() {
        return owner;
    }
}
