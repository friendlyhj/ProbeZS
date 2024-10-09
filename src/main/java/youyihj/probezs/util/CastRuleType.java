package youyihj.probezs.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class CastRuleType implements Type {
    private final List<Type> types = new ArrayList<>();

    public CastRuleType() {
    }

    public List<Type> getTypes() {
        return types;
    }

    public void appendType(Type type) {
        types.add(type);
    }

    @Override
    public String toString() {
        return types.stream().map(Objects::toString).collect(Collectors.joining(", "));
    }
}
