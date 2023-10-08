package youyihj.probezs.core.mixin;


import org.objectweb.asm.ClassWriter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stanhebben.zenscript.compiler.EnvironmentClass;
import stanhebben.zenscript.definitions.zenclasses.ParsedZenClass;
import stanhebben.zenscript.type.ZenTypeZenClass;
import youyihj.probezs.core.hook.ParsedZenClassHooks;

@Mixin(value = ParsedZenClass.class, remap = false)
public abstract class MixinParsedZenClass {

    @Shadow
    @Final
    public ZenTypeZenClass type;

    @Inject(method = "writeMethods", at = @At("HEAD"))
    private void inject_writeMethods_begin(ClassWriter newClass, EnvironmentClass environmentNewClass, CallbackInfo ci) {
        ParsedZenClassHooks.current.set(this.type);
    }

    @Inject(method = "writeConstructors", at = @At("HEAD"))
    private void inject_writeConstructors_begin(ClassWriter newClass, EnvironmentClass environmentNewClass, CallbackInfo ci) {
        ParsedZenClassHooks.current.set(this.type);
    }

    @Inject(method = "writeMethods", at = @At("RETURN"))
    private void inject_writeMethods_end(ClassWriter newClass, EnvironmentClass environmentNewClass, CallbackInfo ci) {
        ParsedZenClassHooks.current.remove();
    }

    @Inject(method = "writeConstructors", at = @At("RETURN"))
    private void inject_writeConstructors_end(ClassWriter newClass, EnvironmentClass environmentNewClass, CallbackInfo ci) {
        ParsedZenClassHooks.current.remove();
    }
}
