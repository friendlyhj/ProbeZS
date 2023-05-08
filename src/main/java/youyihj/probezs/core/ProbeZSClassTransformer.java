package youyihj.probezs.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import youyihj.probezs.core.asm.ASMCraftTweakerAPI;

import java.io.File;
import java.io.IOException;

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
            byte[] result = classWriter.toByteArray();
            try {
                FileUtils.writeByteArrayToFile(new File("CraftTweakerAPI.class"), result);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return result;
        }
        return basicClass;
    }
}
