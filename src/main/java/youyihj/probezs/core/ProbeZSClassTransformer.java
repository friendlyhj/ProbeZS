package youyihj.probezs.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import youyihj.probezs.ProbeZSConfig;
import youyihj.probezs.core.asm.ASMCraftTweakerAPI;

import java.util.Arrays;
import java.util.List;

/**
 * @author youyihj
 */
public class ProbeZSClassTransformer implements IClassTransformer {

    private static final List<String> EXCLUDE_PACKAGE = Arrays.asList(
            "youyihj.probezs",
            "org.spongepowered",
            "net.minecraft"
    );

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null)
            return null;
        if ("crafttweaker.CraftTweakerAPI".equals(name)) {
            ClassWriter classWriter = new ClassWriter(0);
            ASMCraftTweakerAPI asm = new ASMCraftTweakerAPI(Opcodes.ASM5, classWriter);
            new ClassReader(basicClass).accept(asm, 0);
            return classWriter.toByteArray();
        }
        for (String s : EXCLUDE_PACKAGE) {
            if (transformedName.startsWith(s)) {
                return basicClass;
            }
        }
        if (ProbeZSConfig.memberCollector == ProbeZSConfig.MemberCollector.ASM) {
            ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            ASMMemberCollector.MEMBER_FACTORY.putClassNode(classNode);
        }
        return basicClass;
    }
}
