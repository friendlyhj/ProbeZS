package youyihj.probezs.util;

import joptsimple.internal.Strings;

/**
 * @author youyihj
 */
public class IndentStringBuilder {
    private int indentLevel;
    private final int indentSpaceCount;
    private final StringBuilder sb = new StringBuilder();

    public IndentStringBuilder() {
        this(4);
    }

    public IndentStringBuilder(int indentSpaceCount) {
        this.indentSpaceCount = indentSpaceCount;
    }

    public IndentStringBuilder append(String s) {
        sb.append(s);
        return this;
    }

    public IndentStringBuilder nextLine() {
        sb.append("\n");
        sb.append(Strings.repeat(' ', indentSpaceCount * indentLevel));
        return this;
    }

    public IndentStringBuilder interLine() {
        return nextLine().nextLine();
    }

    public IndentStringBuilder push() {
        indentLevel++;
        nextLine();
        return this;
    }

    public IndentStringBuilder pop() {
        indentLevel--;
        nextLine();
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
