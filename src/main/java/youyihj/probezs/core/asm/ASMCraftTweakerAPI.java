package youyihj.probezs.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author youyihj
 */
public class ASMCraftTweakerAPI extends ClassVisitor {
    public ASMCraftTweakerAPI(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals("registerClass")) {
            return new InsertHookMethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions));
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    private static class InsertHookMethodVisitor extends MethodVisitor implements Opcodes {

        public InsertHookMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if ("getDeclaredMethods".equals(name)) {
                super.visitVarInsn(ALOAD, 0);
                super.visitMethodInsn(INVOKESTATIC,
                        "youyihj/probezs/core/asm/CraftTweakerAPIHooks",
                        "readClass",
                        "(Ljava/lang/Class;)V",
                        false);
            }
        }
    }
}
