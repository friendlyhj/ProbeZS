package youyihj.probezs.core.hook;

import java.util.ArrayList;
import java.util.List;

public class Scope {
    private final List<LocalVariable> variables = new ArrayList<>();

    public List<LocalVariable> getVariables() {
        return variables;
    }


}
