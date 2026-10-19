package fr.geming400.screwyou4.killer;

import fr.geming400.screwyou4.killer.parser.Attribute;
import fr.geming400.screwyou4.killer.parser.Infos;
import fr.geming400.screwyou4.killer.parser.PoolEntry;
import net.minecraft.util.RandomSource;

public class MethodTerminator {

    private final Infos.MethodInfo method;
    private final Attribute codeAttribute;
    private final RandomSource random;
    private final String returnType;

    public MethodTerminator(Infos.MethodInfo method, RandomSource random) {
        this.random = random;
        this.method = method;
        Attribute codeAttr = null;
        for (Attribute attribute : this.method.field4.getValue()) {
            if (attribute.field1.<PoolEntry.Utf8PoolEntry>getExpected(this.method.constantPool).getField().getValue().equals("Code")) {
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
//        if (!this.returnType.equals("F")) {
//            throw new UnsupportedOperationException("Method terminator do not supports return type: " + this.returnType);
//        }
        switch (this.returnType) {
            case "V" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                bcg.returnVoid();
                this.codeAttribute.field2.override(bcg.toArray());
            }
            case "Z" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                bcg.pushByte((byte) (this.random.nextBoolean() ? 1 : 0));
                bcg.returnObject();
                this.codeAttribute.field2.override(bcg.toArray());
            }
            case "B" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                bcg.pushByte((byte) this.random.nextInt());
                bcg.returnObject();
                this.codeAttribute.field2.override(bcg.toArray());
            }
            case "S" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                bcg.pushShort((short) this.random.nextInt());
                bcg.returnObject();
                this.codeAttribute.field2.override(bcg.toArray());
            }
            case "C" -> {
                // Doesn't really know if it works but who uses chars anyway ?
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                bcg.pushShort((short) (char) this.random.nextInt());
                bcg.returnObject();
                this.codeAttribute.field2.override(bcg.toArray());
            }
            case "I" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                bcg.pushInt(this.random.nextInt());
                bcg.returnObject();
                this.codeAttribute.field2.override(bcg.toArray());
            }
            case "J" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                bcg.pushLong(this.random.nextLong());
                bcg.returnLong();
                this.codeAttribute.field2.override(bcg.toArray());
            }
            case "D" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                bcg.pushDouble(this.random.nextDouble());
                bcg.returnDouble();
                this.codeAttribute.field2.override(bcg.toArray());
            }
            case "F" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                bcg.pushFloat(this.random.nextFloat());
                bcg.returnFloat();
                this.codeAttribute.field2.override(bcg.toArray());
            }
            default -> throw new UnsupportedOperationException("Method terminator do not supports " + this.returnType + " return type yet!");
        }
    }
}
