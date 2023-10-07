package youyihj.probezs.core.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.symbols.SymbolArgument;
import stanhebben.zenscript.type.ZenType;

@Mixin(value = SymbolArgument.class, remap = false)
public interface SymbolArgumentAccessor {


    @Accessor
    int getId();

    @Accessor
    ZenType getType();

}
