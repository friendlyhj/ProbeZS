package youyihj.probezs.member.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.member.ParameterData;
import youyihj.probezs.util.Arrays;

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
        Type[] types = Type.getType(methodNode.desc).getArgumentTypes();
        return Arrays.map(types, Class.class, memberFactory.getTypeDescResolver()::convertASMType);
    }

    @Override
    public int getParameterCount() {
        TypeResolver typeResolver = memberFactory.getTypeDescResolver();
        if (methodNode.signature != null) {
            return typeResolver.resolveMethodArguments(methodNode.signature).size();
        } else {
            return org.objectweb.asm.Type.getType(methodNode.desc).getArgumentTypes().length;
        }
    }

    @Override
    public int getModifiers() {
        return methodNode.access;
    }

    @Override
    public java.lang.reflect.Type getReturnType() {
        TypeResolver typeResolver = memberFactory.getTypeDescResolver();
        if (methodNode.signature != null) {
            return typeResolver.resolveTypeDesc(typeResolver.resolveMethodReturnType(methodNode.signature));
        } else {
            return typeResolver.resolveTypeDesc(methodNode.desc.substring(methodNode.desc.indexOf(')') + 1));
        }
    }

    @Override
    public ParameterData[] getParameters() {
        ParameterData[] parameterData = new ParameterData[getParameterCount()];
        for (int i = 0; i < parameterData.length; i++) {
            parameterData[i] = new ASMParameter(this, methodNode, i, memberFactory);
        }
        return parameterData;
    }
}
