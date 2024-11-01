package youyihj.probezs.tree;

import com.google.common.collect.ForwardingSet;
import crafttweaker.CraftTweakerAPI;
import youyihj.probezs.tree.primitive.IPrimitiveType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * @author youyihj
 */
public class ImportSet extends ForwardingSet<ZenClassNode> {
    private final ZenClassNode exclude;
    private final Set<ZenClassNode> delegate;

    public ImportSet(ZenClassNode exclude, Set<ZenClassNode> delegate) {
        this.exclude = exclude;
        this.delegate = delegate;
    }

    @Override
    public boolean add(@Nullable ZenClassNode element) {
        if (element instanceof IPrimitiveType || element == exclude) {
            return false;
        } else if (element == null) {
            CraftTweakerAPI.logInfo("[ProbeZS]: null import member in " + exclude.getName());
            return false;
        } else {
            return super.add(element);
        }
    }

    @Override
    public boolean addAll(Collection<? extends ZenClassNode> collection) {
        return standardAddAll(collection);
    }

    @Override
    protected Set<ZenClassNode> delegate() {
        return delegate;
    }
}
