package youyihj.probezs.member.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import youyihj.probezs.member.ParameterData;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author youyihj
 */
public class ASMParameter extends ASMAnnotatedMember implements ParameterData {
    private final MethodNode methodNode;
    private final int index;
    private final ASMMethod method;
    private final String name;

    public ASMParameter(ASMMethod method, MethodNode methodNode, int index, ASMMemberFactory memberFactory, List<AnnotationNode> annotationNode, String name) {
        super(annotationNode, memberFactory);
        this.method = method;
        this.methodNode = methodNode;
        this.index = index;
        this.name = name != null ? name : "arg" + index;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return method.getParameterTypes()[index];
    }

    @Override
    public Type getGenericType() {
        TypeResolver typeResolver = memberFactory.getTypeDescResolver();
        if (methodNode.signature != null) {
            return typeResolver.resolveTypeDesc(typeResolver.resolveMethodArguments(methodNode.signature).get(index));
        } else {
            return getType();
        }
    }

    @Override
    public boolean isVarargs() {
        return (methodNode.access & Opcodes.ACC_VARARGS) != 0 && index == method.getParameterCount() - 1;
    }
}
