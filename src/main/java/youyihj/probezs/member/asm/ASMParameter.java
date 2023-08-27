package youyihj.probezs.member.asm;

import org.objectweb.asm.tree.MethodNode;
import youyihj.probezs.member.ParameterData;

import java.lang.reflect.Type;

/**
 * @author youyihj
 */
public class ASMParameter extends ASMAnnotatedMember implements ParameterData {
    private final MethodNode methodNode;
    private final int index;

    public ASMParameter(MethodNode methodNode, int index, ASMMemberFactory memberFactory) {
        super(null, memberFactory);
        this.methodNode = methodNode;
        this.index = index;
    }

    @Override
    public String getName() {
        return "arg" + index;
    }

    @Override
    public Class<?> getType() {
        org.objectweb.asm.Type[] types = org.objectweb.asm.Type.getType(methodNode.desc).getArgumentTypes();
        try {
            return Class.forName(types[index].getClassName(), false, memberFactory.getClassLoader());
        } catch (ClassNotFoundException e) {
            return Object.class;
        }
    }

    @Override
    public Type getGenericType() {
        String desc = methodNode.signature != null ? methodNode.signature : methodNode.desc;
        return memberFactory.getTypeDescResolver().resolve(org.objectweb.asm.Type.getType(desc).getArgumentTypes()[index].getDescriptor());
    }

    @Override
    public boolean isVarargs() {
        return false;
    }
}
