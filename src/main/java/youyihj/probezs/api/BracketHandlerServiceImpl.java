package youyihj.probezs.api;

import youyihj.probezs.bracket.BracketHandlerCaller;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.rmi.Remote;

/**
 * @author youyihj
 */
public class BracketHandlerServiceImpl implements BracketHandlerService, Remote, Serializable {
    @Nullable
    @Override
    public String getLocalizedName(String expr) {
        return BracketHandlerCaller.INSTANCE.getLocalizedName(expr);
    }

    @Nullable
    @Override
    public String getIcon(String expr) {
        return BracketHandlerCaller.INSTANCE.getIcon(expr);
    }

    @Nonnull
    @Override
    public String getTypeName(String expr) {
        return BracketHandlerCaller.INSTANCE.getTypeName(expr);
    }
}
