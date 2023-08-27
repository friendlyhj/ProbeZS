package youyihj.probezs.member.asm;

import org.objectweb.asm.tree.FieldNode;
import youyihj.probezs.member.FieldData;

import java.lang.reflect.Type;

/**
 * @author youyihj
 */
public class ASMField extends ASMAnnotatedMember implements FieldData {
    private final FieldNode fieldNode;

    private final Class<?> decalredClass;

    public ASMField(FieldNode fieldNode, ASMMemberFactory memberFactory, Class<?> decalredClass) {
        super(fieldNode.visibleAnnotations, memberFactory);
        this.fieldNode = fieldNode;
        this.decalredClass = decalredClass;
    }

    @Override
    public Class<?> getDecalredClass() {
        return decalredClass;
    }

    @Override
    public String getName() {
        return fieldNode.name;
    }

    @Override
    public int getModifiers() {
        return fieldNode.access;
    }

    @Override
    public Type getType() {
        return memberFactory.getTypeDescResolver().resolve(fieldNode.desc);
    }
}
