package youyihj.probezs.core.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stanhebben.zenscript.definitions.ParsedFunction;
import stanhebben.zenscript.statements.Statement;
import youyihj.probezs.core.hook.ParsedFunctionHooks;

@Mixin(value = ParsedFunction.class, remap = false)
public abstract class MixinParsedFunction {

    @Inject(method = "getStatements", at = @At(value = "HEAD"))
    private void inject_getStatements(CallbackInfoReturnable<Statement[]> cir) {
        if (ParsedFunctionHooks.isGeneratingStatement.get()) {
            ParsedFunctionHooks.currentFunction.remove();
            ParsedFunctionHooks.currentFunction.set((ParsedFunction) (Object) this);
        }
    }
}
