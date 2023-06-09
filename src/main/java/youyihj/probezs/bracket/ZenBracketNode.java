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
    public static final int ELEMENTS_ONE_LINE = 5;

    private final List<String> content = new ArrayList<>();
    private final LazyZenClassNode type;
    private final int id;

    public ZenBracketNode(LazyZenClassNode type, int id) {
        this.type = type;
        this.id = id;
    }

    public void addContent(List<String> content) {
        this.content.addAll(content);
    }

    public void addContent(String content) {
        this.content.add(content);
    }

    public String getName() {
        return "bh" + id;
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        if (!type.isExisted()) return;
        sb.append("val ").append(getName()).append(" as ")
                .append(type.get().getName()).append("[] = ");
        if (content.isEmpty()) {
            sb.append("[];");
        } else {
            sb.append("[");
            sb.push();
            Iterator<String> iterator = content.iterator();
            int lineElement = 0;
            String first = iterator.next();
            sb.append("<").append(first).append(">");
            lineElement++;
            while (iterator.hasNext()) {
                sb.append(",");
                if (lineElement == ELEMENTS_ONE_LINE) {
                    sb.nextLine();
                    lineElement = 0;
                } else {
                    sb.append(" ");
                }
                sb.append("<").append(iterator.next()).append(">");
                lineElement++;
            }
            sb.pop();
            sb.append("];");
        }
    }
}
