package youyihj.probezs.member.asm;

import org.objectweb.asm.tree.MethodNode;
import youyihj.probezs.member.ParameterData;


/**
 * @author youyihj
 */
public class ASMParameter extends ASMAnnotatedMember implements ParameterData {
    private final MethodNode methodNode;
    private final int index;
    private final ASMMethod method;

    public ASMParameter(ASMMethod method, MethodNode methodNode, int index, ASMMemberFactory memberFactory) {
        super(null, memberFactory);
        this.method = method;
        this.methodNode = methodNode;
        this.index = index;
    }

    @Override
    public String getName() {
        return "arg" + index;
    }

    @Override
    public Class<?> getType() {
        return method.getParameterTypes()[index];
    }

    @Override
    public java.lang.reflect.Type getGenericType() {
        TypeDescResolver typeDescResolver = memberFactory.getTypeDescResolver();
        if (methodNode.signature != null) {
            return typeDescResolver.resolveTypeDesc(typeDescResolver.resolveMethodArguments(methodNode.signature).get(index));
        } else {
            return getType();
        }
    }

    @Override
    public boolean isVarargs() {
        return false;
    }
}
