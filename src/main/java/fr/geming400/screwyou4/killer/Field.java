package fr.geming400.screwyou4.killer;

import java.nio.ByteBuffer;

public interface Field<N extends Number> {
    N getValue();

    void encode(ByteBuffer buffer);

    abstract class AbstractField<N extends Number> implements Field<N> {

        protected N value;

        public AbstractField(ByteBuffer ignoredBuffer) {
        }

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
    }
}