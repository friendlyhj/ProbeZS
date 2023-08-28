package youyihj.probezs.member.asm;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import youyihj.probezs.core.BytecodeClassLoader;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author youyihj
 */
class TypeDescResolver {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Supplier<BytecodeClassLoader> classLoader;
    private int tokenIndex;

    public TypeDescResolver(ASMMemberFactory memberFactory) {
        this.classLoader = Suppliers.memoize(() -> new BytecodeClassLoader(memberFactory.getClassLoader()));
        results.put("V", Void.class);
        results.put("Z", Boolean.class);
        results.put("C", Character.class);
        results.put("B", Byte.class);
        results.put("I", Integer.class);
        results.put("F", Float.class);
        results.put("J", Long.class);
        results.put("D", Double.class);
    }

    private final Map<String, Type> results = new HashMap<>();

    Type resolveTypeDesc(String desc) {
        return results.computeIfAbsent(desc, this::buildType);
    }

    String[] resolveMethodArguments(String methodSignature) {
        SignatureReader reader = new SignatureReader(methodSignature);
        MethodSignatureTypeNameVisitor visitor = new MethodSignatureTypeNameVisitor();
        reader.accept(visitor);
        return visitor.types.toArray(new String[0]);
    }

    String resolveMethodReturnType(String methodSignature) {
        SignatureReader reader = new SignatureReader(methodSignature);
        MethodSignatureTypeNameVisitor visitor = new MethodSignatureTypeNameVisitor();
        reader.accept(visitor);
        List<String> types = visitor.types;
        return types.get(types.size() - 1);
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

    static class MethodSignatureTypeNameVisitor extends SignatureVisitor {
        final List<String> types = new ArrayList<>();
        StringBuffer buf = new StringBuffer();
        private boolean hasParameters;
        private boolean hasFormals;
        private int argumentStack;
        private boolean collect = true;

        public MethodSignatureTypeNameVisitor() {
            super(ASM5);
        }

        @Override
        public void visitFormalTypeParameter(final String name) {
            if (!hasFormals) {
                hasFormals = true;
                buf.append('<');
            }
            buf.append(name);
            buf.append(':');
        }

        @Override
        public SignatureVisitor visitInterfaceBound() {
            buf.append(':');
            return this;
        }

        @Override
        public SignatureVisitor visitSuperclass() {
            endFormals();
            return this;
        }

        @Override
        public SignatureVisitor visitParameterType() {
            endFormals();
            return this;
        }

        @Override
        public SignatureVisitor visitReturnType() {
            endFormals();
            return this;
        }

        @Override
        public SignatureVisitor visitExceptionType() {
            endFormals();
            collect = false;
            return this;
        }

        @Override
        public void visitBaseType(final char descriptor) {
            finishCurrentType();
            buf.append(descriptor);
        }

        @Override
        public void visitTypeVariable(final String name) {
            finishCurrentType();
            buf.append('T');
            buf.append(name);
            buf.append(';');
        }

        @Override
        public SignatureVisitor visitArrayType() {
            buf.append('[');
            return this;
        }

        @Override
        public void visitClassType(final String name) {
            buf.append('L');
            buf.append(name);
            argumentStack *= 2;
        }

        @Override
        public void visitInnerClassType(final String name) {
            endArguments();
            buf.append('.');
            buf.append(name);
            argumentStack *= 2;
        }

        @Override
        public void visitTypeArgument() {
            if (argumentStack % 2 == 0) {
                ++argumentStack;
                buf.append('<');
            }
            buf.append('*');
        }

        @Override
        public SignatureVisitor visitTypeArgument(final char wildcard) {
            if (argumentStack % 2 == 0) {
                ++argumentStack;
                buf.append('<');
            }
            if (wildcard != '=') {
                buf.append(wildcard);
            }
            return this;
        }

        @Override
        public void visitEnd() {
            endArguments();
            buf.append(';');
        }


        private void finishCurrentType() {
            if (buf.length() != 0) {
                if (collect) {
                    types.add(buf.toString());
                }
                buf = new StringBuffer();
            }
        }

        private void endFormals() {
            if (hasFormals) {
                hasFormals = false;
                buf.append('>');
                finishCurrentType();
            }
        }

        private void endArguments() {
            if (argumentStack % 2 != 0) {
                buf.append('>');
                finishCurrentType();
            }
            argumentStack /= 2;
        }
    }
}
