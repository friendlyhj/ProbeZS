package youyihj.probezs.member.asm;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import youyihj.probezs.core.BytecodeClassLoader;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.DOUBLE;
import static org.objectweb.asm.Type.FLOAT;
import static org.objectweb.asm.Type.LONG;
import static org.objectweb.asm.Type.*;

/**
 * @author youyihj
 */
public class TypeResolver {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Supplier<BytecodeClassLoader> classLoader;
    private final Map<String, Type> results = new HashMap<>();

    private int tokenIndex;

    public TypeResolver(ASMMemberFactory memberFactory) {
        this.classLoader = Suppliers.memoize(() -> new BytecodeClassLoader(memberFactory.getClassLoader()));
        results.put("V", void.class);
        results.put("Z", boolean.class);
        results.put("C", char.class);
        results.put("B", byte.class);
        results.put("I", int.class);
        results.put("F", float.class);
        results.put("J", long.class);
        results.put("D", double.class);
        results.put("S", short.class);
    }

    public Type resolveTypeDesc(String desc) {
        return results.computeIfAbsent(desc, this::buildType);
    }

    public List<String> resolveMethodArguments(String methodSignature) {
        return new MethodParameterParser(methodSignature).parse();
    }

    public String resolveMethodReturnType(String methodSignature) {
        String returnAndException = methodSignature.substring(methodSignature.indexOf(')') + 1);
        if (returnAndException.contains("^")) {
            return methodSignature.substring(0, methodSignature.indexOf('^'));
        } else {
            return returnAndException;
        }
    }

    Class<?> convertASMType(org.objectweb.asm.Type type) {
        try {
            switch (type.getSort()) {
                case VOID:
                    return void.class;
                case BOOLEAN:
                    return boolean.class;
                case BYTE:
                    return byte.class;
                case SHORT:
                    return short.class;
                case INT:
                    return int.class;
                case FLOAT:
                    return float.class;
                case LONG:
                    return long.class;
                case DOUBLE:
                    return double.class;
                case ARRAY:
                    org.objectweb.asm.Type elementType = type.getElementType();
                    int dimensions = type.getDimensions();
                    return Array.newInstance(convertASMType(elementType), new int[dimensions]).getClass();
                case OBJECT:
                    return Class.forName(type.getClassName(), false, classLoader.get());
                default:
                    return Object.class;
            }
        } catch (ClassNotFoundException e) {
            LOGGER.throwing(e);
            return Object.class;
        }
    }

    @SuppressWarnings("all")
    private Type buildType(String desc) {
        String className = "TypeToken" + tokenIndex++;
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classWriter.visit(V1_8, ACC_PUBLIC, className,
                "Lcom/google/common/reflect/TypeToken<" + desc + ">;",
                "com/google/common/reflect/TypeToken", null);
        MethodVisitor constructor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, "com/google/common/reflect/TypeToken", "<init>", "()V", false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
        classWriter.visitEnd();
        classLoader.get().putBytecode(className, classWriter.toByteArray());
        try {
            return ((TypeToken<?>) Class.forName(className, true, classLoader.get()).newInstance()).getType();
        } catch (Throwable e) {
            LOGGER.error("Failed get type from desc: " + desc, e);
            return Object.class;
        }
    }

    static final class MethodParameterParser {
        private int layer = 0;
        private final List<String> paramTypes = new ArrayList<>();
        private StringBuilder sb = new StringBuilder();

        private final String signature;

        public MethodParameterParser(String signature) {
            this.signature = signature;
        }

        List<String> parse() {
            String params = signature.substring(signature.indexOf('(') + 1, signature.indexOf(')'));
            boolean readingLongType = false;

            for (char c : params.toCharArray()) {
                sb.append(c);
                switch (c) {
                    case 'V':
                    case 'Z':
                    case 'C':
                    case 'B':
                    case 'I':
                    case 'F':
                    case 'J':
                    case 'D':
                        if (!readingLongType && layer == 0) {
                            endType();
                        }
                        break;
                    case 'T':
                    case 'L':
                        if (!readingLongType) {
                            readingLongType = true;
                            layer++;
                        }
                        break;
                    case '<':
                    case '>':
                        readingLongType = false;
                        break;
                    case ';':
                        layer--;
                        readingLongType = false;
                        if (layer == 0) {
                            endType();
                        }
                        break;
                }
            }
            return paramTypes;
        }

        private void endType() {
            paramTypes.add(sb.toString());
            sb = new StringBuilder();
        }
    }
}
