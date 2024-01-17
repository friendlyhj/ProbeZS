package youyihj.probezs.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author youyihj
 */
public interface BracketHandlerService {
    @Nullable
    String getLocalizedName(String expr);

    @Nullable
    String getIcon(String expr);

    @Nonnull
    String getTypeName(String expr);
}
