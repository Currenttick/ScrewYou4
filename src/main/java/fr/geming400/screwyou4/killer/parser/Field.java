package fr.geming400.screwyou4.killer.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.function.Function;

public interface Field<N> extends ClassWriteable {

    N getValue();

    void override(N newValue);

    void override(Field<N> field);

    abstract class AbstractField<N> implements Field<N> {

        protected N value;

        public AbstractField(ByteBuffer ignoredBuffer) {}
        
        public AbstractField(N value) {
            this.value = value;
        }

        public String toString() {
            return this.value.getClass().getName() + ":" + this.getValue();
        }

        public N getValue() {
            return this.value;
        }

        @Override
        public void override(N newValue) {
            this.value = newValue;
        }

        @Override
        public void override(Field<N> field) {
            this.value = field.getValue();
        }

        public String format(String fieldName, ConstantPool constantPool) {
            if (this.value instanceof ClassWriteable field) {
                return fieldName + field.format("", constantPool);
            }
            return fieldName + this.value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this.getClass().isInstance(obj)) {
                return this.value.equals(this.getClass().cast(obj).value);
            }
            return false;
        }
    }

    class ByteField extends AbstractField<Byte> {

        public ByteField(byte b) {
            super(b);
        }

        public ByteField(ByteBuffer buffer) {
            super(buffer.get());
        }

        @Override
        public void write(ByteBuffer buffer) {
            buffer.put(this.value);
        }

        @Override
        public int size() {
            return 1;
        }
    }

    class ShortField extends AbstractField<Short> {

        public ShortField(ByteBuffer buffer) {
            super(buffer.getShort());
        }
        
        public ShortField(short value) {
            super(value);
        }

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putShort(this.value);
        }

        @Override
        public int size() {
            return 2;
        }
    }

    class IntField extends AbstractField<Integer> {

        public IntField(ByteBuffer buffer) {
            super(buffer.getInt());
        }
        
        public IntField(int value) {
            super(value);
        }

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(this.value);
        }

        @Override
        public int size() {
            return 4;
        }
    }

    class LongField extends AbstractField<Long> {

        public LongField(ByteBuffer buffer) {
            super(buffer.getLong());
        }
        
        public LongField(long value) {
            super(value);
        }

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putLong(this.value);
        }

        @Override
        public int size() {
            return 8;
        }
    }

    class FloatField extends AbstractField<Float> {

        public FloatField(ByteBuffer buffer) {
            super(buffer.getFloat());
        }

        public FloatField(float value) {
            super(value);
        }

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putFloat(this.value);
        }

        @Override
        public int size() {
            return 4;
        }
    }

    class DoubleField extends AbstractField<Double> {

        public DoubleField(ByteBuffer buffer) {
            super(buffer.getDouble());
        }

        public DoubleField(double value) {
            super(value);
        }

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putDouble(this.value);
        }

        @Override
        public int size() {
            return 8;
        }
    }

    class Utf8Field extends AbstractField<String> {

        public Utf8Field(ByteBuffer buffer) {
            super(buffer);
            byte[] bytes = new byte[buffer.getShort()];
            buffer.get(bytes);
            this.value = new String(bytes);
        }
        
        public Utf8Field(String value) {
            super(value);
        }

        @Override
        public void write(ByteBuffer buffer) {
            byte[] bytes = this.value.getBytes();
            buffer.putShort((short) bytes.length);
            buffer.put(bytes);
        }

        @Override
        public int size() {
            return 2 + this.value.getBytes().length;
        }
    }

    class ShortPrefixedArrayField<F extends ClassWriteable> extends AbstractField<ArrayList<F>> {

        public ShortPrefixedArrayField(ByteBuffer buffer, Function<ByteBuffer, F> factory) {
            super(buffer);
            short length = buffer.getShort();
            this.value = new ArrayList<>(length);
            for (short i = 0; i < length; i++) {
                this.value.add(factory.apply(buffer));
            }
        }

        public ShortPrefixedArrayField(ArrayList<F> value) {
            super(value);
        }

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putShort((short) this.value.size());
            this.value.forEach(field -> field.write(buffer));
        }

        public F get(int index) {
            return this.value.get(index);
        }

        public F get(short index) {
            return this.value.get(index);
        }

        @Override
        public int size() {
            return 2 + this.value.stream().mapToInt(F::size).sum();
        }

        @Override
        public String format(String fieldName, ConstantPool constantPool) {
            StringBuilder sb = new StringBuilder(fieldName + "[");
            this.value.forEach(field -> sb.append(field.format("", constantPool)).append(", "));
            if (!this.value.isEmpty()) {
                sb.delete(sb.length() - 2, sb.length());
            }
            return sb.append("]").toString();
        }
    }

    class ConstantPoolEntriesArray extends ShortPrefixedArrayField<PoolEntry> {
        public ConstantPoolEntriesArray(ByteBuffer buffer) {
            super(ByteBuffer.wrap(new byte[]{0, 0}), PoolEntry::read);
            short length = buffer.getShort();
            this.value = new ArrayList<>(length);
            this.value.add(new PoolEntry.UnkownPoolEntry());
            for (short i = 1; i < length; i++) {
                PoolEntry entry = PoolEntry.read(buffer);
                this.value.add(entry);
                if (entry instanceof PoolEntry.DoublePoolEntry | entry instanceof PoolEntry.LongPoolEntry) {
                    this.value.add(new PoolEntry.UnkownPoolEntry());
                    i++;
                }
            }
        }

        public void append(PoolEntry entry) {
            this.value.add(entry);
            if (entry instanceof PoolEntry.DoublePoolEntry | entry instanceof PoolEntry.LongPoolEntry) {
                this.value.add(new PoolEntry.UnkownPoolEntry());
            }
        }
    }

    class IntPrefixedArrayField<F extends AbstractField<?>> extends AbstractField<ArrayList<F>> {

        public IntPrefixedArrayField(ByteBuffer buffer, Function<ByteBuffer, F> factory) {
            super(buffer);
            int length = buffer.getInt();
            this.value = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                this.value.add(factory.apply(buffer));
            }
        }

        public IntPrefixedArrayField(ArrayList<F> value) {
            super(value);
        }

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(this.value.size());
            this.value.forEach(field -> field.write(buffer));
        }

        public F get(int index) {
            return this.value.get(index);
        }

        @Override
        public int size() {
            return 4 + this.value.stream().mapToInt(F::size).sum();
        }

        @Override
        public String format(String fieldName, ConstantPool constantPool) {
            StringBuilder sb = new StringBuilder(fieldName + "[");
            this.value.forEach(field -> sb.append(field.format("", constantPool)).append(", "));
            if (!this.value.isEmpty()) {
                sb.delete(sb.length() - 2, sb.length());
            }
            return sb.append("]").toString();
        }
    }

    class ConstantPoolReferenceField extends ShortField {

        public ConstantPoolReferenceField(ByteBuffer buffer) {
            super(buffer);
        }

        public ConstantPoolReferenceField(short value) {
            super(value);
        }

        @Override
        public String format(String fieldName, ConstantPool constantPool) {
            return fieldName + constantPool.get(this.value).format("", constantPool);
        }

        public PoolEntry get(ConstantPool constantPool) {
            return constantPool.get(this.value);
        }

        public <P extends PoolEntry> P getExpected(ConstantPool constantPool) {
            return constantPool.getExpected(this.value);
        }
    }

    abstract class FieldElement<F extends ClassWriteable> implements ClassWriteable {

        public final F field1;

        public FieldElement(F field1) {
            this.field1 = field1;
        }

        public FieldElement(ByteBuffer buffer, Function<ByteBuffer, F> factory1) {
            this.field1 = factory1.apply(buffer);
        }

        @Override
        public void write(ByteBuffer buffer) {
            this.field1.write(buffer);
        }

        @Override
        public String format(String name, ConstantPool constantPool) {
            return "{" + name + this.field1.format(this.fieldName1(), constantPool) + "}";
        }

        public abstract String fieldName1();

        @Override
        public int size() {
            return this.field1.size();
        }
    }

    abstract class FieldElement2<F1 extends ClassWriteable, F2 extends ClassWriteable> extends FieldElement<F1> {

        public final F2 field2;

        public FieldElement2(F1 field1, F2 field2) {
            super(field1);
            this.field2 = field2;
        }

        public FieldElement2(ByteBuffer buffer, Function<ByteBuffer, F1> factory1, Function<ByteBuffer, F2> factory2) {
            super(buffer, factory1);
            this.field2 = factory2.apply(buffer);
        }

        @Override
        public void write(ByteBuffer buffer) {
            super.write(buffer);
            this.field2.write(buffer);
        }

        @Override
        public String format(String name, ConstantPool constantPool) {
            return "{" + name + this.field1.format(this.fieldName1(), constantPool) + ", " + this.field2.format(this.fieldName2(), constantPool) + "}";
        }

        public abstract String fieldName2();

        @Override
        public int size() {
            return super.size() + this.field2.size();
        }
    }

    abstract class FieldElement3<F1 extends ClassWriteable, F2 extends ClassWriteable, F3 extends ClassWriteable> extends FieldElement2<F1, F2> {

        public final F3 field3;

        public FieldElement3(F1 field1, F2 field2, F3 field3) {
            super(field1, field2);
            this.field3 = field3;
        }

        public FieldElement3(ByteBuffer buffer, Function<ByteBuffer, F1> factory1, Function<ByteBuffer, F2> factory2, Function<ByteBuffer, F3> factory3) {
            super(buffer, factory1, factory2);
            this.field3 = factory3.apply(buffer);
        }

        @Override
        public void write(ByteBuffer buffer) {
            super.write(buffer);
            this.field3.write(buffer);
        }

        @Override
        public String format(String name, ConstantPool constantPool) {
            return "{" + name + this.field1.format(this.fieldName1(), constantPool) + ", " + this.field2.format(this.fieldName2(), constantPool) + ", " + this.field3.format(this.fieldName3(), constantPool) + "}";
        }

        public abstract String fieldName3();

        @Override
        public int size() {
            return super.size() + this.field3.size();
        }
    }

    abstract class FieldElement4<F1 extends ClassWriteable, F2 extends ClassWriteable, F3 extends ClassWriteable, F4 extends ClassWriteable> extends FieldElement3<F1, F2, F3> {

        public final F4 field4;

        public FieldElement4(F1 field1, F2 field2, F3 field3, F4 field4) {
            super(field1, field2, field3);
            this.field4 = field4;
        }

        public FieldElement4(ByteBuffer buffer, Function<ByteBuffer, F1> factory1, Function<ByteBuffer, F2> factory2, Function<ByteBuffer, F3> factory3, Function<ByteBuffer, F4> factory4) {
            super(buffer, factory1, factory2, factory3);
            this.field4 = factory4.apply(buffer);
        }

        @Override
        public void write(ByteBuffer buffer) {
            super.write(buffer);
            this.field4.write(buffer);
        }

        @Override
        public String format(String name, ConstantPool constantPool) {
            return "{" + name + this.field1.format(this.fieldName1(), constantPool) + ", " + this.field2.format(this.fieldName2(), constantPool) + ", " + this.field3.format(this.fieldName3(), constantPool) + ", " + this.field4.format(this.fieldName4(), constantPool) + "}";
        }

        public abstract String fieldName4();

        @Override
        public int size() {
            return super.size() + this.field4.size();
        }
    }
}
