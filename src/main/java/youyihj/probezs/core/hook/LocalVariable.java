package youyihj.probezs.core.hook;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.util.function.IntSupplier;

public class LocalVariable {

    private final String name;
    private final Type type;

    private final IntSupplier idxSupplier;

    private Label scopeBegin;
    private Label scopeEnd;

    public LocalVariable(String name, Type type, IntSupplier idxSupplier) {
        this.name = name;
        this.type = type;
        this.idxSupplier = idxSupplier;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public IntSupplier getIdxSupplier() {
        return idxSupplier;
    }

    public Label getScopeBegin() {
        return scopeBegin;
    }

    public void setScopeBegin(Label scopeBegin) {
        this.scopeBegin = scopeBegin;
    }

    public Label getScopeEnd() {
        return scopeEnd;
    }

    public void setScopeEnd(Label scopeEnd) {
        this.scopeEnd = scopeEnd;
    }
}
