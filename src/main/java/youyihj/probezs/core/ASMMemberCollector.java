package youyihj.probezs.core;

import com.google.common.collect.Sets;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.member.asm.ASMMemberFactory;

/**
 * @author youyihj
 */
public class ASMMemberCollector {
    public static final ASMMemberFactory MEMBER_FACTORY = new ASMMemberFactory(Sets.newHashSet("ZenClass", "ZenExpansion"), () -> ASMMemberCollector.Holder::getMainLoader);

    public static class Holder {
        public static ClassLoader getMainLoader() {
            return ProbeZS.class.getClassLoader();
        }
    }
}
