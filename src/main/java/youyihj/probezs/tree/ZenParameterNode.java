package youyihj.probezs.tree;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;
import stanhebben.zenscript.annotations.Optional;
import youyihj.probezs.util.IndentStringBuilder;
import youyihj.probezs.util.ZenKeywords;
import youyihj.zenutils.impl.member.ExecutableData;
import youyihj.zenutils.impl.member.LiteralType;
import youyihj.zenutils.impl.member.TypeData;
import youyihj.zenutils.impl.member.bytecode.BytecodeAnnotatedMember;
import youyihj.zenutils.impl.member.bytecode.BytecodeMethodData;
import youyihj.zenutils.impl.member.reflect.ReflectionExecutableData;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class ZenParameterNode implements IZenDumpable, ITypeNameContextAcceptor {
    private final Supplier<String> name;
    private final Supplier<JavaTypeMirror.Result> type;
    private final Optional optional;
    private final boolean varArgs;

    public ZenParameterNode(Supplier<String> name, Supplier<JavaTypeMirror.Result> type, Optional optional, boolean varArgs) {
        this.name = name;
        this.type = type;
        this.optional = optional;
        this.varArgs = varArgs;
    }

    public static ZenParameterNode read(ExecutableData method, int index, TypeData parameterType, ZenClassTree tree) {
        boolean varArgs = method.isVarArgs() && index == method.parameterCount() - 1;
        JavaTypeMirror returnType;
        if (varArgs && parameterType.descriptor().startsWith("[")) {
            returnType = tree.createJavaTypeMirror(new LiteralType(parameterType.descriptor().substring(1)));
        } else {
            returnType = tree.createJavaTypeMirror(parameterType.javaType());
        }
        Supplier<String> name = () -> {
            List<String> list = tree.getMappings().find(method);
            if (list != null && index < list.size()) {
                return list.get(index);
            }
            if (method instanceof ReflectionExecutableData) {
                Executable executable = ObfuscationReflectionHelper.getPrivateValue(ReflectionExecutableData.class, (ReflectionExecutableData) method, "executable");
                return executable.getParameters()[index].getName();
            } else if (method instanceof BytecodeMethodData) {
                MethodNode methodNode = ObfuscationReflectionHelper.getPrivateValue(BytecodeMethodData.class, (BytecodeMethodData) method, "methodNode");
                if (methodNode.parameters != null && index < methodNode.parameters.size()) {
                    ParameterNode parameterNode = methodNode.parameters.get(index);
                    if (parameterNode.name != null && !parameterNode.name.isEmpty()) {
                        return parameterNode.name;
                    }
                }
            }
            return "arg" + index;
        };
        Optional optional = null;
        if (method instanceof ReflectionExecutableData) {
            Executable executable = ObfuscationReflectionHelper.getPrivateValue(ReflectionExecutableData.class, (ReflectionExecutableData) method, "executable");
            optional = executable.getParameters()[index].getAnnotation(Optional.class);
        } else if (method instanceof BytecodeMethodData) {
            MethodNode methodNode = ObfuscationReflectionHelper.getPrivateValue(BytecodeMethodData.class, (BytecodeMethodData) method, "methodNode");
            optional = new BytecodeAnnotatedMember() {
                {
                    if (methodNode.visibleParameterAnnotations != null && index < methodNode.visibleParameterAnnotations.length) {
                        setAnnotationNodes(methodNode.visibleParameterAnnotations[index]);
                    }
                    if (methodNode.invisibleParameterAnnotations != null && index < methodNode.invisibleParameterAnnotations.length) {
                        setAnnotationNodes(methodNode.invisibleParameterAnnotations[index]);
                    }
                }
            }.getAnnotation(Optional.class);
        }

        return new ZenParameterNode(name, returnType, optional, varArgs);
    }

    public static List<ZenParameterNode> read(ExecutableData method, int startIndex, ZenClassTree tree) {
        List<TypeData> parameters = method.parameters();
        List<ZenParameterNode> parameterNodes = new ArrayList<>(method.parameterCount() - startIndex);
        for (int i = startIndex; i < method.parameterCount(); i++) {
            parameterNodes.add(ZenParameterNode.read(method, i, parameters.get(i), tree));
        }
        return parameterNodes;
    }

    public String getName() {
        String name = this.name.get();
        if (ZenKeywords.is(name)) {
            name = "_" + name;
        }
        if (varArgs) {
            name = "..." + name;
        }
        return name;
    }

    public JavaTypeMirror.Result getType() {
        return type.get();
    }

    public boolean isExisted() {
        if (type instanceof JavaTypeMirror) {
            return ((JavaTypeMirror) type).isExisted();
        } else {
            return true;
        }
    }

    public Optional getOptional() {
        return optional;
    }

    @Override
    public void toZenScript(IndentStringBuilder sb, TypeNameContext context) {
        String typeName = context.getTypeName(getType());
        sb.append(getName()).append(" as ").append(typeName);
        if (optional != null) {
            sb.append(" = ");
            switch (typeName) {
                case "int":
                case "short":
                case "long":
                case "byte":
                    sb.append(String.valueOf(optional.valueLong()));
                    break;
                case "bool":
                    sb.append(String.valueOf(optional.valueBoolean()));
                    break;
                case "float":
                case "double":
                    sb.append(String.valueOf(optional.valueDouble()));
                    break;
                case "string":
                    if (optional.value().isEmpty()) {
                        sb.append(null);
                    } else {
                        sb.append("\"").append(optional.value()).append("\"");
                    }
                    break;
                default:
                    if (optional.methodClass() == Optional.class) {
                        sb.append(null);
                    } else {
                        sb.append("default");
                    }
                    break;
            }
        }
    }

    @Override
    public void setMentionedTypes(TypeNameContext context) {
        context.addClasses(getType().getTypeVariables());
    }
}
