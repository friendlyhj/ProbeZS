package youyihj.probezs.core.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import stanhebben.zenscript.compiler.EnvironmentMethod;
import stanhebben.zenscript.compiler.IEnvironmentClass;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.symbols.SymbolArgument;
import stanhebben.zenscript.symbols.SymbolLocal;
import stanhebben.zenscript.util.MethodOutput;
import stanhebben.zenscript.util.ZenPosition;
import youyihj.probezs.core.hook.ILocalVariableMethodOutput;
import youyihj.probezs.core.hook.LocalVariable;
import youyihj.probezs.core.hook.ParsedZenClassHooks;

import java.util.HashMap;

@Mixin(value = EnvironmentMethod.class, remap = false)
public abstract class MixinEnvironmentMethod {

    @Shadow
    public abstract MethodOutput getOutput();

    @Shadow
    @Final
    private HashMap<SymbolLocal, Integer> locals;

    @Unique
    public int probeZS$getLocalDirect(SymbolLocal variable) {
        if (!locals.containsKey(variable)) {
            return -1;
        }
        return locals.get(variable);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void inject_init(MethodOutput output, IEnvironmentClass environment, CallbackInfo ci) {
        ((ILocalVariableMethodOutput) output).probeZS$enableLocalVariable();
        if (ParsedZenClassHooks.current.get() != null) {
            ((ILocalVariableMethodOutput) output).probeZS$injectThis(ParsedZenClassHooks.current.get().zenClass);
        }
    }

    @Inject(method = "putValue",
            at = @At("HEAD"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void inject_addVar(String name, IZenSymbol value, ZenPosition position, CallbackInfo ci) {
        ILocalVariableMethodOutput output = (ILocalVariableMethodOutput) this.getOutput();

        if (value instanceof SymbolLocal) {
            SymbolLocal local = (SymbolLocal) value;
            LocalVariable localVariable = new LocalVariable(name, ((SymbolLocal) value).getType().toASMType(), () -> probeZS$getLocalDirect(local));
            output.probeZS$addVariable(localVariable);
        }

        if (value instanceof SymbolArgument) {
            SymbolArgument args = (SymbolArgument) value;
            LocalVariable localVariable = new LocalVariable(name, ((SymbolArgumentAccessor) args).getType().toASMType(), () -> ((SymbolArgumentAccessor) args).getId());
            output.probeZS$addVariable(localVariable);
        }
    }

}
