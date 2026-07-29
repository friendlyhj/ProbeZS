package youyihj.probezs.member.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.member.ParameterData;
import youyihj.probezs.util.Arrays;

import java.util.ArrayList;
import java.util.List;

/**
 * @author youyihj
 */
public class ASMMethod extends ASMAnnotatedMember implements ExecutableData {
    private final MethodNode methodNode;
    private final Class<?> declaredClass;
    private List<String> parameterNames;

    public ASMMethod(MethodNode methodNode, ASMMemberFactory memberFactory, Class<?> declaredClass) {
        super(methodNode.visibleAnnotations, memberFactory);
        this.methodNode = methodNode;
        this.declaredClass = declaredClass;
    }

    @Override
    public String getName() {
        return methodNode.name;
    }

    @Override
    public Class<?> getDeclaredClass() {
        return declaredClass;
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
        List<String> paramNames = extractParameterNames();

        for (int i = 0; i < parameterData.length; i++) {
            List<AnnotationNode>[] parameterAnnotations = methodNode.visibleParameterAnnotations;
            List<AnnotationNode> annotationNodes = parameterAnnotations != null ? parameterAnnotations[i] : null;

            String paramName = (paramNames != null && i < paramNames.size()) ? paramNames.get(i) : null;
            parameterData[i] = new ASMParameter(this, methodNode, i, memberFactory, annotationNodes, paramName);
        }
        return parameterData;
    }

    /**
     * Extracts parameter names from bytecode LocalVariableTable
     * LVT doesn't always exist, example abstract methods or methods intended to be overwritten as lambdas.
     * Falls back to ParameterNode names, then to "arg" + index.
     * @return list of parameter names
     */
    private List<String> extractParameterNames() {
        if (parameterNames == null) {
            int paramCount = getParameterCount();
            Type[] parameterTypes = Type.getType(methodNode.desc).getArgumentTypes();
            parameterNames = new ArrayList<>(paramCount);

            // Try to get names from LocalVariableTable
            List<LocalVariableNode> lvt = methodNode.localVariables;
            if (lvt != null && !lvt.isEmpty()) {
                int localVariableIndex = (methodNode.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
                for (int i = 0; i < parameterTypes.length && i < paramCount; i++) {
                    Type parameterType = parameterTypes[i];
                    String nameFromBytecode = null;
                    for (LocalVariableNode localVariableNode : lvt) {
                        if (localVariableNode.index == localVariableIndex) {
                            nameFromBytecode = localVariableNode.name;
                            break;
                        }
                    }
                    localVariableIndex += parameterType.getSize();
                    parameterNames.add(nameFromBytecode != null ? nameFromBytecode : "arg" + i);
                }
            } else if (methodNode.parameters != null && methodNode.parameters.size() >= paramCount) {
                // Fallback to ParameterNode names
                for (int i = 0; i < paramCount; i++) {
                    parameterNames.add(methodNode.parameters.get(i).name);
                }
            } else {
                // Last resort: default names
                for (int i = 0; i < paramCount; i++)
                    parameterNames.add("arg" + i);
            }

            // Ensure the list has the correct size by filling up with argX
            while (parameterNames.size() < paramCount)
                parameterNames.add("arg" + parameterNames.size());
        }
        return parameterNames;
    }
}
