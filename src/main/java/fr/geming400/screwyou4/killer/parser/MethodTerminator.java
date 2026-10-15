package fr.geming400.screwyou4.killer.parser;

import com.google.common.primitives.Ints;
import fr.geming400.screwyou4.ScrewYou4;

import java.nio.ByteBuffer;

import static fr.geming400.screwyou4.killer.ClassJudger.log;

public class MethodTerminator {

    private final Infos.MethodInfo method;
    private final Attribute codeAttribute;
    private final String returnType;

    public MethodTerminator(Infos.MethodInfo method) {
        this.method = method;
        Attribute codeAttr = null;
        for (Attribute attribute : this.method.field4.value) {
            if (attribute.field1.<PoolEntry.Utf8PoolEntry>getExpected(this.method.constantPool).field.value.equals("Code")) {
                codeAttr = attribute;
                break;
            }
        }
        if (this.method.getName().contains("<init>") || this.method.getName().contains("<clinit>")) {
            throw new UnsupportedOperationException("Method terminator do not supports constructors yet!");
        }
        if (codeAttr == null) {
            throw new IllegalArgumentException("Code attribute not found");
        }
        this.codeAttribute = codeAttr;
        String desc = this.method.getDesc();
        this.returnType = desc.substring(desc.indexOf(")") + 1);
    }

    public void terminate() {
        if (this.returnType.equals("V")) {
            int size = this.codeAttribute.field2.value.size();
            int codeLength = Ints.fromBytes(this.codeAttribute.field2.value.get(4).value, this.codeAttribute.field2.value.get(5).value, this.codeAttribute.field2.value.get(6).value, this.codeAttribute.field2.value.get(7).value);
            ByteBuffer buffer = ByteBuffer.allocate(17);
            buffer.putInt(13);
            for (int i  = 0; i < 4; i++) {
                buffer.put(this.codeAttribute.field2.value.get(i).value);
            }
            buffer.putInt(1);
            buffer.put((byte) 0xB1);
            buffer.putInt(0);
//            for (int i  = 8 + codeLength; i < size; i++) {
//                buffer.put(this.codeAttribute.field2.value.get(i).value);
//            }
            this.codeAttribute.field2.override(
                    new Field.IntPrefixedArrayField<>(buffer.flip(), Field.ByteField::new)
            );
            log(this.codeAttribute.field2.format("", this.method.constantPool));
            ScrewYou4.LOGGER.info("Method terminator has terminated {}!", this.method.getName());
        } else {
            log("Better luck next time! " + this.returnType);
            throw new UnsupportedOperationException("Method terminator do not supports " + this.returnType + " yet!");
        }
    }
}
