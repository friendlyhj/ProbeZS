package youyihj.probezs.util;

/**
 * @author youyihj
 */
@SuppressWarnings("UnusedReturnValue")
public class IndentStringBuilder {
    private int indentLevel;
    private final int indentSpaceCount;
    private final StringBuilder sb = new StringBuilder();

    private boolean emptyLine = true;
    private boolean interLine = false;

    public IndentStringBuilder() {
        this(4);
    }

    public IndentStringBuilder(int indentSpaceCount) {
        this.indentSpaceCount = indentSpaceCount;
    }

    public IndentStringBuilder append(String s) {
        if (interLine) {
            forceNextLine();
            interLine = false;
        }
        sb.append(s);
        emptyLine = false;
        return this;
    }

    public IndentStringBuilder nextLine() {
        if (!emptyLine) {
            forceNextLine();
        }
        return this;
    }

    public IndentStringBuilder forceNextLine() {
        sb.append(System.lineSeparator());
        int spaceCount = indentSpaceCount * indentLevel;
        for (int i = 0; i < spaceCount; i++) {
            sb.append(' ');
        }
        emptyLine = true;
        return this;
    }

    public IndentStringBuilder interLine() {
        nextLine();
        interLine = true;
        return this;
    }

    public IndentStringBuilder push() {
        indentLevel++;
        interLine = false;
        forceNextLine();
        return this;
    }

    public IndentStringBuilder pop() {
        indentLevel--;
        interLine = false;
        forceNextLine();
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
