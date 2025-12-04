package youyihj.probezs.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import youyihj.probezs.core.asm.ASMCraftTweakerAPI;

import java.util.Arrays;
import java.util.List;

/**
 * @author youyihj
 */
public class ProbeZSClassTransformer implements IClassTransformer {

    private static final List<String> EXCLUDE_PACKAGES = Arrays.asList(
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
        for (String excludePackage : EXCLUDE_PACKAGES) {
            if (transformedName.startsWith(excludePackage)) {
                return basicClass;
            }
        }
        return basicClass;
    }
}
