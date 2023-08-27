package youyihj.probezs.member.asm;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.core.BytecodeClassLoader;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author youyihj
 */
class TypeDescResolver {
    private final Supplier<BytecodeClassLoader> classLoader;
    private int tokenIndex;

    public TypeDescResolver(ASMMemberFactory memberFactory) {
        this.classLoader = Suppliers.memoize(() -> new BytecodeClassLoader(memberFactory.getClassLoader()));
        results.put("V", void.class);
        results.put("Z", boolean.class);
        results.put("C", char.class);
        results.put("B", byte.class);
        results.put("I", int.class);
        results.put("F", float.class);
        results.put("J", long.class);
        results.put("D", double.class);
    }

    private final Map<String, Type> results = new HashMap<>();

    Type resolve(String desc) {
        return results.computeIfAbsent(desc, this::buildType);
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
            ProbeZS.logger.error("Failed get type from desc: " + desc, e);
            return Object.class;
        }
    }
}
