package youyihj.probezs.tree;

import com.google.common.collect.Lists;
import youyihj.probezs.util.IndentStringBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author youyihj
 */
public class ZenAnnotationNode implements IZenDumpable {
    private final HashMap<String, List<String>> values = new HashMap<>();

    public void add(String head, String value) {
        values.computeIfAbsent(head, it -> Lists.newArrayList(value));
    }

    public void add(String head) {
        add(head, "");
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        if (!values.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : values.entrySet()) {
                sb.append("#").append(entry.getKey());
                List<String> aoValues = entry.getValue();
                if (aoValues.size() != 1 || !aoValues.get(0).isEmpty()) {
                    sb.append(" ");
                    Iterator<String> valueIterator = aoValues.iterator();
                    while (valueIterator.hasNext()) {
                        sb.append(valueIterator.next());
                        if (valueIterator.hasNext()) {
                            sb.append(" ");
                        }
                    }
                }
                sb.nextLine();
            }
        }
    }
}
