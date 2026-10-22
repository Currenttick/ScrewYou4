package fr.geming400.screwyou4.killer;

import fr.geming400.screwyou4.killer.parser.Attribute;
import fr.geming400.screwyou4.killer.parser.Infos;
import fr.geming400.screwyou4.killer.parser.PoolEntry;
import net.minecraft.util.RandomSource;

import java.util.UUID;

public class MethodTerminator {

    private final Infos.MethodInfo method;
    private final Attribute codeAttribute;
    private final RandomSource random;
    private final String overrideReturn;

    public MethodTerminator(Infos.MethodInfo method, RandomSource random, String overrideReturn) {
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
        this.overrideReturn = overrideReturn;
    }

    public void terminate() {
//        if (!this.returnType.equals("Ljava/lang/String;")) {
//            throw new UnsupportedOperationException("Method terminator do not supports return type: " + this.returnType);
//        }

        switch (this.method.getReturnType()) {
            case "V" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                bcg.returnVoid();

                this.codeAttribute.field2.override(bcg.toArray());
            }

            case "Z" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                if (this.overrideReturn != null) {
                    bcg.pushByte((byte) (Boolean.parseBoolean(this.overrideReturn) ? 1 : 0));
                } else {
                    bcg.pushByte((byte) (this.random.nextBoolean() ? 1 : 0));
                }

                bcg.returnPrimitive();
                this.codeAttribute.field2.override(bcg.toArray());
            }

            case "B" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                if (this.overrideReturn != null) {
                    bcg.pushByte(Byte.parseByte(this.overrideReturn));
                } else {
                    bcg.pushByte((byte) this.random.nextInt());
                }

                bcg.returnPrimitive();
                this.codeAttribute.field2.override(bcg.toArray());
            }

            case "S" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                if (this.overrideReturn != null) {
                    bcg.pushShort(Short.parseShort(this.overrideReturn));
                } else {
                    bcg.pushShort((short) this.random.nextInt());
                }

                bcg.returnPrimitive();
                this.codeAttribute.field2.override(bcg.toArray());
            }

            case "C" -> {
                // Doesn't really know if it works but who uses chars anyway ?
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                if (this.overrideReturn != null) {
                    bcg.pushShort((short) this.overrideReturn.charAt(0));
                } else {
                    bcg.pushShort((short) (char) this.random.nextInt());
                }

                bcg.returnPrimitive();
                this.codeAttribute.field2.override(bcg.toArray());
            }

            case "I" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                if (this.overrideReturn != null) {
                    bcg.pushInt(Integer.parseInt(this.overrideReturn));
                } else {
                    bcg.pushInt(this.random.nextInt());
                }

                bcg.returnPrimitive();
                this.codeAttribute.field2.override(bcg.toArray());
            }

            case "J" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                if (this.overrideReturn != null) {
                    bcg.pushLong(Long.parseLong(this.overrideReturn));
                } else {
                    bcg.pushLong(this.random.nextLong());
                }

                bcg.returnLong();
                this.codeAttribute.field2.override(bcg.toArray());
            }

            case "D" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                if (this.overrideReturn != null) {
                    bcg.pushDouble(Double.parseDouble(this.overrideReturn));
                } else {
                    bcg.pushDouble(this.random.nextDouble());
                }

                bcg.returnDouble();
                this.codeAttribute.field2.override(bcg.toArray());
            }

            case "F" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                if (this.overrideReturn != null) {
                    bcg.pushFloat(Float.parseFloat(this.overrideReturn));
                } else {
                    bcg.pushFloat(this.random.nextFloat());
                }

                bcg.returnFloat();
                this.codeAttribute.field2.override(bcg.toArray());
            }

            case "Ljava/lang/String;" -> {
                BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                if (this.overrideReturn != null) {
                    bcg.pushString(this.overrideReturn);
                } else {
                    bcg.pushString(new UUID(this.random.nextLong(), this.random.nextLong()).toString());
                }

                bcg.returnObject();
                this.codeAttribute.field2.override(bcg.toArray());
            }

            default -> {
                if ("null".equals(this.overrideReturn)) {
                    BytecodeGenerator bcg = new BytecodeGenerator(this.method, this.method.constantPool);
                    bcg.pushNull();
                    bcg.returnObject();
                    this.codeAttribute.field2.override(bcg.toArray());
                    return;
                }
                throw new UnsupportedOperationException("Method terminator do not supports " + this.method.getReturnType() + " return type yet!");
            }
        }
    }
}
