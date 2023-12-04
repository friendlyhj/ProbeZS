package youyihj.probezs.core;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

/**
 * @author youyihj
 */
public class LateMixinInit implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList(
                "mixins.probezs.json"
        );
    }
}
