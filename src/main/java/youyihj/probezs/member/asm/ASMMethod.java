package youyihj.probezs.member.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.member.ParameterData;
import youyihj.probezs.util.Arrays;

import java.lang.reflect.Array;

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
        return Arrays.map(types, Class.class, this::convertASMType);
    }

    @Override
    public int getParameterCount() {
        TypeDescResolver typeDescResolver = memberFactory.getTypeDescResolver();
        if (methodNode.signature != null) {
            return typeDescResolver.resolveMethodArguments(methodNode.signature).size();
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
        TypeDescResolver typeDescResolver = memberFactory.getTypeDescResolver();
        if (methodNode.signature != null) {
            return typeDescResolver.resolveTypeDesc(typeDescResolver.resolveMethodReturnType(methodNode.signature));
        } else {
            return typeDescResolver.resolveTypeDesc(methodNode.desc.substring(methodNode.desc.indexOf(')') + 1));
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

    private Class<?> convertASMType(Type type) {
        try {
            switch (type.getSort()) {
                case Type.VOID:
                    return void.class;
                case Type.BOOLEAN:
                    return boolean.class;
                case Type.BYTE:
                    return byte.class;
                case Type.SHORT:
                    return short.class;
                case Type.INT:
                    return int.class;
                case Type.FLOAT:
                    return float.class;
                case Type.LONG:
                    return long.class;
                case Type.DOUBLE:
                    return double.class;
                case Type.ARRAY:
                    org.objectweb.asm.Type elementType = type.getElementType();
                    int dimensions = type.getDimensions();
                    return Array.newInstance(convertASMType(elementType), new int[dimensions]).getClass();
                case Type.OBJECT:
                    return Class.forName(type.getClassName(), false, memberFactory.getClassLoader());
                default:
                    return Object.class;
            }
        } catch (ClassNotFoundException e) {
            return Object.class;
        }
    }
}
