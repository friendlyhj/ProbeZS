package youyihj.probezs.tree;

import youyihj.probezs.tree.primitive.IPrimitiveType;
import youyihj.probezs.util.IndentStringBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author youyihj
 */
public class TypeNameContext implements IZenDumpable {
    private final ZenClassNode classDeclaration;
    private final Map<String, ZenClassNode> classes = new HashMap<>();
    private final Map<ZenClassNode, String> classNames = new TreeMap<>();

    public TypeNameContext(ZenClassNode classDeclaration) {
        this.classDeclaration = classDeclaration;
    }

    public void addClass(ZenClassNode classNode) {
        String qualifiedName = classNode.getQualifiedName();
        if (classNames.containsKey(classNode)) {
            return;
        }
        if (!classes.containsKey(qualifiedName)) {
            classes.put(qualifiedName, classNode);
            classNames.put(classNode, qualifiedName);
        } else {
            int suffix = 0;
            while (classes.containsKey(qualifiedName + suffix)) {
                suffix++;
            }
            classes.put(qualifiedName + suffix, classNode);
            classNames.put(classNode, qualifiedName + suffix);
        }
    }

    public void addClasses(List<ZenClassNode> classNodes) {
        for (ZenClassNode classNode : classNodes) {
            addClass(classNode);
        }
    }

    public String getTypeName(ZenClassNode classNode) {
        return classNames.get(classNode);
    }

    public String getTypeName(JavaTypeMirror.Result result) {
        return result.getQualifiedName(this);
    }

    @Override
    public void toZenScript(IndentStringBuilder sb, TypeNameContext context) {
        classes.forEach((name, clazz) -> {
            if (clazz instanceof IPrimitiveType || clazz == classDeclaration) {
                return;
            }
            sb.append("import ").append(clazz.getName());
            if (!clazz.getQualifiedName().equals(name)) {
                sb.append(" as ").append(name);
            }
            sb.append(";").nextLine();
        });
    }
}
