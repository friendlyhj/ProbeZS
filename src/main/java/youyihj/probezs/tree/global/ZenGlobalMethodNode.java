package youyihj.probezs.tree.global;

import youyihj.probezs.ProbeZS;
import youyihj.probezs.ProbeZSConfig;
import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.member.ParameterData;
import youyihj.probezs.tree.*;
import youyihj.probezs.util.IndentStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author youyihj
 */
public class ZenGlobalMethodNode extends ZenExecutableNode implements IZenDumpable, ITypeNameContextAcceptor, Comparable<ZenGlobalMethodNode> {
    private final String name;
    private final JavaTypeMirror returnType;
    private final List<ZenParameterNode> parameters;

    public ZenGlobalMethodNode(String name, JavaTypeMirror returnType, List<ZenParameterNode> parameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public static ZenGlobalMethodNode read(String name, ExecutableData method, ZenClassTree tree) {
        JavaTypeMirror returnType = tree.createJavaTypeMirror(method.getReturnType());

        ParameterData[] parameters = method.getParameters();
        List<ZenParameterNode> parameterNodes = new ArrayList<>(method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            parameterNodes.add(ZenParameterNode.read(method, i, parameters[i], tree));
        }
        ZenGlobalMethodNode globalMethodNode = new ZenGlobalMethodNode(name, returnType, parameterNodes);
        if (ProbeZSConfig.outputSourceExpansionMembers) {
            String classOwner = ProbeZS.instance.getClassOwner(method.getDecalredClass());
            if (!"crafttweaker".equals(classOwner)) {
                globalMethodNode.setOwner(classOwner);
            }
        }
        return globalMethodNode;
    }

    @Override
    public void setMentionedTypes(TypeNameContext context) {
        context.addClasses(returnType.get().getTypeVariables());
        for (ZenParameterNode parameter : parameters) {
            parameter.setMentionedTypes(context);
        }
    }

    @Override
    public int compareTo(ZenGlobalMethodNode o) {
        return name.compareTo(o.name);
    }

    @Override
    protected boolean existed() {
        return true;
    }

    @Override
    protected void writeModifiersAndName(IndentStringBuilder sb) {
        sb.append("global function ").append(name);
    }

    @Override
    protected List<ZenParameterNode> getParameters() {
        return parameters;
    }

    @Override
    protected JavaTypeMirror.Result getReturnType() {
        return returnType.get();
    }
}
