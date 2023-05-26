package youyihj.probezs.bracket;

import youyihj.probezs.tree.IZenDumpable;
import youyihj.probezs.tree.LazyZenClassNode;
import youyihj.probezs.util.IndentStringBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author youyihj
 */
public class ZenBracketNode implements IZenDumpable {
    private final List<String> content = new ArrayList<>();
    private final LazyZenClassNode type;
    private final String regex;
    private final int id;

    public ZenBracketNode(LazyZenClassNode type, String regex, int id) {
        this.type = type;
        this.regex = regex;
        this.id = id;
    }

    public void addContent(List<String> content) {
        this.content.addAll(content);
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        if (!type.isExisted()) return;
        sb.append("#brackets: ").append(regex).nextLine();
        sb.append("val ").append("bh").append(String.valueOf(id)).append(" as ")
                .append(type.get().getName()).append("[] = ");
        if (content.isEmpty()) {
            sb.append("[];");
        } else {
            sb.append("[");
            sb.push();
            Iterator<String> iterator = content.iterator();
            String first = iterator.next();
            sb.append("<").append(first).append(">");
            while (iterator.hasNext()) {
                sb.append(",").nextLine().append(iterator.next());
            }
            sb.pop();
            sb.append("];");
        }
    }
}
