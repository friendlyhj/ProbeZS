package youyihj.probezs.core.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stanhebben.zenscript.compiler.IEnvironmentMethod;
import stanhebben.zenscript.statements.StatementBlock;
import stanhebben.zenscript.statements.StatementForeach;
import youyihj.probezs.core.hook.ILocalVariableMethodOutput;

@Mixin(value = {
        StatementBlock.class,
        StatementForeach.class
}, remap = false)
public abstract class MixinStatementBlockOrForeach {

    @Inject(method = "compile(Lstanhebben/zenscript/compiler/IEnvironmentMethod;)V", at = @At("HEAD"))
    private void inject_compile_start(IEnvironmentMethod environment, CallbackInfo ci) {
        ILocalVariableMethodOutput output = (ILocalVariableMethodOutput) environment.getOutput();
        output.probeZS$beginScope();
    }

    @Inject(method = "compile(Lstanhebben/zenscript/compiler/IEnvironmentMethod;)V", at = @At("RETURN"))
    private void inject_compile_return(IEnvironmentMethod environment, CallbackInfo ci) {
        ILocalVariableMethodOutput output = (ILocalVariableMethodOutput) environment.getOutput();
        output.probeZS$endScope();
    }
}
