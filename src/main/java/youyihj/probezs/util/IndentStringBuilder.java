package youyihj.probezs.util;

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
        sb.append(System.lineSeparator());
        int spaceCount = indentSpaceCount * indentLevel;
        for (int i = 0; i < spaceCount; i++) {
            sb.append(' ');
        }
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
