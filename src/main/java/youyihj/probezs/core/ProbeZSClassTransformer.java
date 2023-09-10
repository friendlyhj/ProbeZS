package youyihj.probezs.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import youyihj.probezs.ProbeZSConfig;
import youyihj.probezs.core.asm.ASMCraftTweakerAPI;

/**
 * @author youyihj
 */
public class ProbeZSClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if ("crafttweaker.CraftTweakerAPI".equals(name)) {
            ClassWriter classWriter = new ClassWriter(0);
            ASMCraftTweakerAPI asm = new ASMCraftTweakerAPI(Opcodes.ASM5, classWriter);
            new ClassReader(basicClass).accept(asm, 0);
            return classWriter.toByteArray();
        }
        if (!name.startsWith("youyihj.probezs.") && !name.startsWith("org.spongepowered") && ProbeZSConfig.memberCollector == ProbeZSConfig.MemberCollector.ASM) {
            ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            ASMMemberCollector.MEMBER_FACTORY.putClassNode(classNode);
        }
        return basicClass;
    }
}
