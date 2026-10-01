package fr.geming400.screwyou4.killer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public interface Field<N extends Number> extends ClassWriteable {

    N getValue();

    abstract class AbstractField<N extends Number> implements Field<N> {

        protected N value;

        public AbstractField(ByteBuffer ignoredBuffer) {}

        @Override
        public N getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.value.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this.getClass().isInstance(obj)) {
                return this.value.equals(this.getClass().cast(obj).value);
            }
            return false;
        }
    }

    abstract class ArrayField<F extends ClassWriteable> implements Field<Integer> {

        protected final ArrayList<F> value = new ArrayList<>();

        public ArrayField(ByteBuffer ignoredBuffer) {}

        public F getField(short index) {
            return this.value.get(index);
        }

        @Override
        public void encode(ByteBuffer buffer) {
            this.encodeHeader(buffer);
            for (F field : this.value) {
                field.encode(buffer);
            }
        }

        protected void encodeHeader(ByteBuffer buffer) {
            buffer.putShort((short) this.value.size());
        }

        @Override
        public Integer getValue() {
            return this.size();
        }

        @Override
        public int size() {
            return 2 + this.value.stream().mapToInt(F::size).sum();
        }

        @Override
        public String toString() {
            return this.value.size() + this.value.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this.getClass().isInstance(obj)) {
                return this.value.equals(this.getClass().cast(obj).value);
            }
            return false;
        }
    }

    class BigByteArrayField extends ArrayField<ByteField> {

        public BigByteArrayField(ByteBuffer buffer) {
            super(buffer);
            int size = buffer.getShort();
            for (int i = 0; i < size; i++) {
                this.value.add(new ByteField(buffer));
            }
        }

        @Override
        protected void encodeHeader(ByteBuffer buffer) {
            buffer.putInt(this.value.size());
        }

        @Override
        public int size() {
            return 2 + this.value.size();
        }
    }

    class ShortArrayField extends ArrayField<ShortField> {

        public ShortArrayField(ByteBuffer buffer) {
            super(buffer);
            short size = buffer.getShort();
            for (short i = 0; i < size; i++) {
                this.value.add(new ShortField(buffer));
            }
        }
    }

    class AttributeArrayField extends ArrayField<AttributeInfo> {

        public AttributeArrayField(ByteBuffer buffer, ArrayList<PoolEntry> poolEntries) {
            super(buffer);
            short size = buffer.getShort();
            for (short i = 0; i < size; i++) {
                this.value.add(new AttributeInfo(buffer, poolEntries));
            }
        }
    }

    class FieldArrayField extends ArrayField<FieldInfo> {

        public FieldArrayField(ByteBuffer buffer, ArrayList<PoolEntry> poolEntries) {
            super(buffer);
            short size = buffer.getShort();
            for (short i = 0; i < size; i++) {
                this.value.add(new FieldInfo(buffer, poolEntries));
            }
        }
    }

    class MethodArrayField extends ArrayField<MethodInfo> {

        public MethodArrayField(ByteBuffer buffer, ArrayList<PoolEntry> poolEntries) {
            super(buffer);
            short size = buffer.getShort();
            for (short i = 0; i < size; i++) {
                this.value.add(new MethodInfo(buffer, poolEntries));
            }
        }
    }

    class Utf8Field extends AbstractField<Short> {
        protected String utf8;

        public Utf8Field(ByteBuffer buffer) {
            super(buffer);
            this.value = buffer.getShort();
            byte[] utf8Bytes = new byte[this.value];
            buffer.get(utf8Bytes);
            this.utf8 = new String(utf8Bytes);
        }

        @Override
        public void encode(ByteBuffer buffer) {
            buffer.putShort(this.value);
            buffer.put(this.utf8.getBytes());
        }

        @Override
        public int size() {
            return 2 + this.value;
        }

        @Override
        public String toString() {
            return '"' + this.utf8 + '"';
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Utf8Field other) {
                return this.utf8.equals(other.utf8);
            }
            return false;
        }
    }

    class IntField extends AbstractField<Integer> {

        public IntField(ByteBuffer buffer) {
            super(buffer);
            this.value = buffer.getInt();
        }

        @Override
        public void encode(ByteBuffer buffer) {
            buffer.putInt(this.value);
        }

        @Override
        public int size() {
            return 4;
        }
    }

    class ShortField extends AbstractField<Short> {
        public ShortField(ByteBuffer buffer) {
            super(buffer);
            this.value = buffer.getShort();
        }

        @Override
        public void encode(ByteBuffer buffer) {
            buffer.putShort(this.value);
        }

        @Override
        public int size() {
            return 2;
        }
    }

    class ByteField extends AbstractField<Byte> {
        public ByteField(ByteBuffer buffer) {
            super(buffer);
            this.value = buffer.get();
        }

        @Override
        public void encode(ByteBuffer buffer) {
            buffer.put(this.value);
        }

        @Override
        public int size() {
            return 1;
        }
    }

    class LongField extends AbstractField<Long> {

        public LongField(ByteBuffer buffer) {
            super(buffer);
            this.value = buffer.getLong();
        }

        @Override
        public void encode(ByteBuffer buffer) {
            buffer.putLong(this.value);
        }

        @Override
        public int size() {
            return 8;
        }
    }

    class FloatField extends AbstractField<Float> {
        public FloatField(ByteBuffer buffer) {
            super(buffer);
            this.value = buffer.getFloat();
        }

        @Override
        public void encode(ByteBuffer buffer) {
            buffer.putFloat(this.value);
        }

        @Override
        public int size() {
            return 4;
        }
    }

    class DoubleField extends AbstractField<Double> {
        public DoubleField(ByteBuffer buffer) {
            super(buffer);
            this.value = buffer.getDouble();
        }

        @Override
        public void encode(ByteBuffer buffer) {
            buffer.putDouble(this.value);
        }

        @Override
        public int size() {
            return 8;
        }
    }
}