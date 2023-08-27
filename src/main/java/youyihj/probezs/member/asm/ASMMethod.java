package youyihj.probezs.member.asm;

import org.objectweb.asm.tree.MethodNode;
import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.member.ParameterData;
import youyihj.probezs.util.Arrays;

import java.lang.reflect.Type;

/**
 * @author youyihj
 */
public class ASMMethod extends ASMAnnotatedMember implements ExecutableData {
    private final MethodNode methodNode;
    private final Class<?> decalredClass;

    public ASMMethod(MethodNode methodNode, ASMMemberFactory memberFactory, Class<?> decalredClass) {
        super(methodNode.visibleAnnotations, memberFactory);
        this.methodNode = methodNode;
        this.decalredClass = decalredClass;
    }

    @Override
    public String getName() {
        return methodNode.name;
    }

    @Override
    public Class<?> getDecalredClass() {
        return decalredClass;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        org.objectweb.asm.Type[] types = org.objectweb.asm.Type.getType(methodNode.desc).getArgumentTypes();
        return Arrays.map(types, Class.class, it -> {
            try {
                return Class.forName(it.getClassName(), false, memberFactory.getClassLoader());
            } catch (ClassNotFoundException e) {
                return Object.class;
            }
        });
    }

    @Override
    public int getParameterCount() {
        return methodNode.parameters == null ? 0 : methodNode.parameters.size();
    }

    @Override
    public int getModifiers() {
        return methodNode.access;
    }

    @Override
    public Type getReturnType() {
        String desc = methodNode.signature != null ? methodNode.signature : methodNode.desc;
        return memberFactory.getTypeDescResolver().resolve(org.objectweb.asm.Type.getType(desc).getReturnType().getDescriptor());
    }

    @Override
    public ParameterData[] getParameters() {
        ParameterData[] parameterData = new ParameterData[getParameterCount()];
        for (int i = 0; i < getParameterCount(); i++) {
            parameterData[i] = new ASMParameter(methodNode, i, memberFactory);
        }
        return parameterData;
    }
}
