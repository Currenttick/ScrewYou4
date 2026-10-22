package fr.geming400.screwyou4.killer;

import fr.geming400.screwyou4.killer.parser.ConstantPool;
import fr.geming400.screwyou4.killer.parser.Field;
import fr.geming400.screwyou4.killer.parser.Infos;
import fr.geming400.screwyou4.killer.parser.PoolEntry;

import java.lang.classfile.Opcode;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class BytecodeGenerator {

    private final Infos.MethodInfo method;
    private final ConstantPool constantPool;
    private short maxStack;
    private short maxLocals;
    private final ArrayList<Byte> bytes = new ArrayList<>();

    public BytecodeGenerator(Infos.MethodInfo method, ConstantPool constantPool) {
        this.method = method;
        this.constantPool = constantPool;
        String desc = method.getDesc();
        this.maxLocals = BytecodeUtils.getParametersSize(desc.substring(desc.indexOf("(") + 1, desc.lastIndexOf(")")), true);
        this.maxLocals++;
        this.maxStack = this.maxLocals;
    }

    public Field.IntPrefixedArrayField<Field.ByteField> toArray() {
        ByteBuffer buffer = ByteBuffer.allocate(16 + this.bytes.size());
        buffer.putInt(12 + this.bytes.size());
        buffer.putShort(this.maxStack);
        buffer.putShort(this.maxLocals);
        buffer.putInt(this.bytes.size());
        for (Byte b : this.bytes) {
            buffer.put(b);
        }
        buffer.putInt(0);
        return new Field.IntPrefixedArrayField<>(buffer.flip(), Field.ByteField::new);
    }

    public void returnVoid() {
        this.bytes.add((byte) Opcode.RETURN.bytecode());
    }

    public void returnPrimitive() {
        this.bytes.add((byte) Opcode.IRETURN.bytecode());
    }

    public void returnObject() {
        this.bytes.add((byte) Opcode.ARETURN.bytecode());
    }

    public void returnLong() {
        this.bytes.add((byte) Opcode.LRETURN.bytecode());
    }

    public void returnFloat() {
        this.bytes.add((byte) Opcode.FRETURN.bytecode());
    }

    public void returnDouble() {
        this.bytes.add((byte) Opcode.DRETURN.bytecode());
    }

    public void loadConstant(short index) {
        this.maxLocals++;
        this.maxStack++;
        this.bytes.add((byte) Opcode.LDC_W.bytecode());
        this.bytes.add((byte) (index >> 8));
        this.bytes.add((byte) index);
    }

    public void loadLongConstant(short index) {
        this.maxLocals++;
        this.maxStack++;
        this.maxLocals++;
        this.maxStack++;
        this.bytes.add((byte) Opcode.LDC2_W.bytecode());
        this.bytes.add((byte) (index >> 8));
        this.bytes.add((byte) index);
    }

    public void pushByte(Byte b) {
        this.maxLocals++;
        this.maxStack++;
        this.bytes.add((byte) Opcode.BIPUSH.bytecode());
        this.bytes.add(b);
    }

    public void pushShort(short s) {
        this.maxLocals++;
        this.maxStack++;
        this.bytes.add((byte) Opcode.SIPUSH.bytecode());
        this.bytes.add((byte) (s >> 8));
        this.bytes.add((byte) s);
    }

    public void pushInt(int i) {
        short index = this.constantPool.getIndex(
                poolEntry -> poolEntry instanceof PoolEntry.IntPoolEntry intPoolEntry && intPoolEntry.getField().getValue() == i,
                () -> new PoolEntry.IntPoolEntry(i));
        this.loadConstant(index);
    }

    public void pushLong(long l) {
        short index = this.constantPool.getIndex(
                poolEntry -> poolEntry instanceof PoolEntry.LongPoolEntry longPoolEntry && longPoolEntry.getField().getValue() == l,
                () -> new PoolEntry.LongPoolEntry(l));
        this.loadLongConstant(index);
    }

    public void pushFloat(float f) {
        short index = this.constantPool.getIndex(
                poolEntry -> poolEntry instanceof PoolEntry.FloatPoolEntry floatPoolEntry && floatPoolEntry.getField().getValue() == f,
                () -> new PoolEntry.FloatPoolEntry(f));
        this.loadConstant(index);
    }

    public void pushDouble(double d) {
        short index = this.constantPool.getIndex(
                poolEntry -> poolEntry instanceof PoolEntry.DoublePoolEntry doublePoolEntry && doublePoolEntry.getField().getValue() == d,
                () -> new PoolEntry.DoublePoolEntry(d));
        this.loadLongConstant(index);
    }

    public void pushNull() {
        this.maxLocals++;
        this.maxStack++;
        this.bytes.add((byte) Opcode.ACONST_NULL.bytecode());
    }

    public void pushString(String string) {
        this.maxLocals++;
        this.maxStack++;
        short index = this.constantPool.getIndex(
                poolEntry -> poolEntry instanceof PoolEntry.StringPoolEntry stringPoolEntry && stringPoolEntry.getField().<PoolEntry.Utf8PoolEntry>getExpected(this.constantPool).getField().getValue().equals(string),
                () -> new PoolEntry.StringPoolEntry(
                        this.constantPool.getIndex(
                                poolEntry -> poolEntry instanceof PoolEntry.Utf8PoolEntry utf8PoolEntry && utf8PoolEntry.getField().getValue().equals(string),
                                () -> new PoolEntry.Utf8PoolEntry(
                                        string
                                )
                        )
                ));
        this.loadConstant(index);
    }
}
