package youyihj.probezs.tree;

import youyihj.probezs.util.IndentStringBuilder;

/**
 * @author youyihj
 */
public class ZenExpandClassNode extends ZenClassNode {
    public ZenExpandClassNode(String name, ZenClassTree tree) {
        super(name, tree);
    }

    @Override
    public void toZenScript(IndentStringBuilder sb, TypeNameContext context) {
        sb.append("expand ");
        sb.append(getName());
        sb.append(" {");
        sb.push();
        for (ZenPropertyNode propertyNode : properties.values()) {
            sb.nextLine();
            propertyNode.toZenScript(sb, context);
        }
        if (!properties.isEmpty()) {
            sb.interLine();
        }
        for (ZenConstructorNode constructor : constructors) {
            constructor.toZenScript(sb, context);
            sb.interLine();
        }
        for (ZenMemberNode member : members) {
            member.toZenScript(sb, context);
            sb.interLine();
        }
        for (ZenOperatorNode operator : operators.values()) {
            operator.toZenScript(sb, context);
            sb.interLine();
        }
        sb.pop();
        sb.append("}");
    }
}
