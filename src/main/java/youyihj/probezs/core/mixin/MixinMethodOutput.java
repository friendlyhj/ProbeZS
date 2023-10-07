package youyihj.probezs.core.mixin;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import stanhebben.zenscript.definitions.zenclasses.ParsedZenClass;
import stanhebben.zenscript.util.MethodOutput;
import stanhebben.zenscript.util.ZenPosition;
import youyihj.probezs.core.hook.ILocalVariableMethodOutput;
import youyihj.probezs.core.hook.LocalVariable;
import youyihj.probezs.core.hook.ParsedFunctionHooks;
import youyihj.probezs.core.hook.Scope;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mixin(value = MethodOutput.class, remap = false)
public abstract class MixinMethodOutput implements ILocalVariableMethodOutput {

    @Shadow
    @Final
    private LocalVariablesSorter visitor;

    @Shadow
    public abstract void label(Label label);

    @Unique
    private Label probeZS$firstLabel;
    @Unique
    private Label probeZS$lastLabel;

    @Unique
    private final ArrayDeque<Scope> probeZS$scopes = new ArrayDeque<>();
    @Unique
    private final List<LocalVariable> probeZS$variables = new ArrayList<>();
    @Unique
    private final List<LocalVariable> probeZS$unfinishedVariables = new ArrayList<>();
    @Unique
    private boolean probeZS$enabled = false;


    @Unique
    public void probeZS$beginScope() {
        if (!probeZS$enabled) {
            return;
        }
        probeZS$scopes.push(new Scope());
    }

    @Unique
    public void probeZS$endScope() {
        if (!probeZS$enabled) {
            return;
        }
        Scope scope = probeZS$scopes.pop();
        for (LocalVariable variable : scope.getVariables()) {
            if (variable.getScopeBegin() == null) {
                variable.setScopeBegin(probeZS$firstLabel);
            }
            probeZS$unfinishedVariables.add(variable);
        }
    }

    @Unique
    private void probeZS$finishVariables() {
        if (!probeZS$enabled) {
            return;
        }
        for (LocalVariable unfinishedVariable : probeZS$unfinishedVariables) {
            unfinishedVariable.setScopeEnd(probeZS$lastLabel);
            probeZS$variables.add(unfinishedVariable);
        }
        probeZS$unfinishedVariables.clear();
    }


    @Override
    public void probeZS$enableLocalVariable() {
        this.probeZS$scopes.push(new Scope());
        this.probeZS$enabled = true;
    }

    @Inject(method = {"ret", "returnType"}, at = @At(value = "HEAD"))
    private void inject_ret(CallbackInfo ci) {

        if (!probeZS$enabled) {
            return;
        }

        if (probeZS$firstLabel == null) {
            probeZS$firstLabel = new Label();
            visitor.visitLabel(probeZS$firstLabel);

            if (ParsedFunctionHooks.currentFunction.get() != null) {
                ZenPosition position = ParsedFunctionHooks.currentFunction.get().getPosition();
                visitor.visitLineNumber(position.getLine(), probeZS$firstLabel);
            }
        }
    }

    @Inject(method = "start", at = @At(value = "HEAD"))
    private void inject_start(CallbackInfo ci) {
        ParsedFunctionHooks.isGeneratingStatement.set(true);
    }

    @Inject(method = "end", at = @At(value = "HEAD"))
    private void inject_end(CallbackInfo ci) {
        ParsedFunctionHooks.currentFunction.remove();
        ParsedFunctionHooks.isGeneratingStatement.set(false);
        if (!probeZS$enabled) {
            return;
        }

        probeZS$endScope();

        probeZS$lastLabel = new Label();

        label(probeZS$lastLabel);


        probeZS$finishVariables();

        for (LocalVariable variable : probeZS$variables) {
            String name = variable.getName();
            String desc = variable.getType().getDescriptor();
            int local = variable.getIdxSupplier().getAsInt();
            Label begin = variable.getScopeBegin();
            if (begin == null) {
                begin = this.probeZS$firstLabel;
            }
            Label end = variable.getScopeEnd();
            if (begin != null && end != null && begin != end) {
                visitor.visitLocalVariable(name, desc, null, begin, end, local);
            }
        }
    }

    @Inject(method = "position",
            at = @At(value = "INVOKE", target = "Lorg/objectweb/asm/commons/LocalVariablesSorter;visitLabel(Lorg/objectweb/asm/Label;)V"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void inject_position(ZenPosition position, CallbackInfo ci, Label label) {
        if (probeZS$firstLabel == null) {
            probeZS$firstLabel = label;
        }

        probeZS$lastLabel = label;

        probeZS$finishVariables();
    }

    @Override
    public void probeZS$addVariable(LocalVariable variable) {
        if (!probeZS$enabled) {
            return;
        }

        variable.setScopeBegin(probeZS$lastLabel);

        Objects.requireNonNull(probeZS$scopes.peek()).getVariables().add(variable);
    }

    @Override
    public void probeZS$injectThis(ParsedZenClass zenClass) {
        if (!probeZS$enabled) {
            return;
        }
        Scope rootScope = probeZS$scopes.peekLast();
        if (zenClass != null && rootScope != null) {
            // add 'this'
            Type asmType = zenClass.type.toASMType();
            LocalVariable thisVar = new LocalVariable("this", asmType, () -> 0);
            rootScope.getVariables().add(thisVar);
        }
    }
}
