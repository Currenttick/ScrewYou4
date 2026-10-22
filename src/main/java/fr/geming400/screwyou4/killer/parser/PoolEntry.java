package fr.geming400.screwyou4.killer.parser;

import fr.geming400.screwyou4.entrypoints.ScrewYou4;

import java.nio.ByteBuffer;

public abstract class PoolEntry implements ClassWriteable {

    public static PoolEntry read(ByteBuffer buffer) {
        byte prefix = buffer.get();
        return switch (prefix) {
            case 1 -> new Utf8PoolEntry(buffer);
            case 3 -> new IntPoolEntry(buffer);
            case 4 -> new FloatPoolEntry(buffer);
            case 5 -> new LongPoolEntry(buffer);
            case 6 -> new DoublePoolEntry(buffer);
            case 7 -> new ClassPoolEntry(buffer);
            case 8 -> new StringPoolEntry(buffer);
            case 9 -> new FieldReferencePoolEntry(buffer);
            case 10 -> new MethodReferencePoolEntry(buffer);
            case 11 -> new InterfaceMethodReferencePoolEntry(buffer);
            case 12 -> new NameAndTypePoolEntry(buffer);
            case 15 -> new MethodHandlePoolEntry(buffer);
            case 16 -> new MethodTypePoolEntry(buffer);
            case 18 -> new InvokeDynamicPoolEntry(buffer);
            default -> new UnkownPoolEntry(prefix);
        };
    }

    public static class UnkownPoolEntry extends PoolEntry {
        public UnkownPoolEntry(byte type) {
            ScrewYou4.LOGGER.error("Unknown pool entry : {}", type);
        }

        public UnkownPoolEntry() {}

        @Override
        public void write(ByteBuffer buffer) {}

        @Override
        public String format(String name, ConstantPool constantPool) {
            return "UNKNOWN";
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof UnkownPoolEntry;
        }
    }

    private abstract static class OneFieldPoolEntry<F extends Field<?>> extends PoolEntry {

        protected final F field;

        protected OneFieldPoolEntry(F field) {
            this.field = field;
        }

        @Override
        public String format(String name, ConstantPool constantPool) {
            return name + this.field.format(this.fieldName(), constantPool);
        }

        @Override
        public void write(ByteBuffer buffer) {
            buffer.put(this.getId());
            this.field.write(buffer);
        }

        protected abstract byte getId();

        protected String fieldName() {
            return "";
        }

        @Override
        public int size() {
            return 1 + this.field.size();
        }

        public F getField() {
            return this.field;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OneFieldPoolEntry<?> other) {
                return this.field.equals(other.field);
            }
            return false;
        }
    }

    private abstract static class OneReferencePoolEntry extends OneFieldPoolEntry<Field.ConstantPoolReferenceField> {
        protected OneReferencePoolEntry(ByteBuffer buffer) {
            super(new Field.ConstantPoolReferenceField(buffer));
        }

        protected OneReferencePoolEntry(short index) {
            super(new Field.ConstantPoolReferenceField(index));
        }

        @Override
        protected String fieldName() {
            return super.fieldName();
        }
    }

    private abstract static class TwoFieldsPoolEntry<F1 extends Field<?>, F2 extends Field<?>> extends PoolEntry {

        protected final F1 field1;
        protected final F2 field2;

        private TwoFieldsPoolEntry(F1 field1, F2 field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        @Override
        public String format(String name, ConstantPool constantPool) {
            return name + this.field1.format(this.fieldName1(), constantPool) + this.field2.format(this.fieldName2(), constantPool);
        }

        @Override
        public void write(ByteBuffer buffer) {
            buffer.put(this.getId());
            this.field1.write(buffer);
            this.field2.write(buffer);
        }

        protected abstract byte getId();

        public String fieldName1() {
            return "";
        }

        public String fieldName2() {
            return "";
        }

        @Override
        public int size() {
            return 1 + this.field1.size() + this.field2.size();
        }

        public F1 getField1() {
            return this.field1;
        }

        public F2 getField2() {
            return this.field2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TwoFieldsPoolEntry<?, ?> other) {
                return this.field1.equals(other.field1) && this.field2.equals(other.field2);
            }
            return false;
        }
    }

    private abstract static class TwoReferencesPoolEntry extends TwoFieldsPoolEntry<Field.ConstantPoolReferenceField, Field.ConstantPoolReferenceField> {

        private TwoReferencesPoolEntry(ByteBuffer buffer) {
            super(new Field.ConstantPoolReferenceField(buffer), new Field.ConstantPoolReferenceField(buffer));
        }
    }

    public static class Utf8PoolEntry extends OneFieldPoolEntry<Field.Utf8Field> {

        public Utf8PoolEntry(ByteBuffer buffer) {
            super(new Field.Utf8Field(buffer));
        }

        public Utf8PoolEntry(String value) {
            super(new Field.Utf8Field(value));
        }

        @Override
        protected byte getId() {
            return 1;
        }
    }

    public static class IntPoolEntry extends OneFieldPoolEntry<Field.IntField> {

        public IntPoolEntry(ByteBuffer buffer) {
            super(new Field.IntField(buffer));
        }

        public IntPoolEntry(int value) {
            super(new Field.IntField(value));
        }

        protected byte getId() {
            return 3;
        }
    }

    public static class FloatPoolEntry extends OneFieldPoolEntry<Field.FloatField> {

        public FloatPoolEntry(ByteBuffer buffer) {
            super(new Field.FloatField(buffer));
        }

        public FloatPoolEntry(float value) {
            super(new Field.FloatField(value));
        }

        protected byte getId() {
            return 4;
        }
    }

    public static class LongPoolEntry extends OneFieldPoolEntry<Field.LongField> {

        public LongPoolEntry(ByteBuffer buffer) {
            super(new Field.LongField(buffer));
        }

        public LongPoolEntry(long value) {
            super(new Field.LongField(value));
        }

        protected byte getId() {
            return 5;
        }
    }

    public static class DoublePoolEntry extends OneFieldPoolEntry<Field.DoubleField> {

        public DoublePoolEntry(ByteBuffer buffer) {
            super(new Field.DoubleField(buffer));
        }

        public DoublePoolEntry(double value) {
            super(new Field.DoubleField(value));
        }

        protected byte getId() {
            return 6;
        }
    }

    public static class ClassPoolEntry extends OneReferencePoolEntry {

        public ClassPoolEntry(ByteBuffer buffer) {
            super(buffer);
        }

        @Override
        protected byte getId() {
            return 7;
        }

        @Override
        protected String fieldName() {
            return "class : ";
        }
    }

    public static class StringPoolEntry extends OneReferencePoolEntry {

        public StringPoolEntry(ByteBuffer buffer) {
            super(buffer);
        }

        public StringPoolEntry(short value) {
            super(value);
        }

        @Override
        protected byte getId() {
            return 8;
        }

        @Override
        protected String fieldName() {
            return "ref : ";
        }
    }

    public static class FieldReferencePoolEntry extends TwoReferencesPoolEntry {

        public FieldReferencePoolEntry(ByteBuffer buffer) {
            super(buffer);
        }

        protected byte getId() {
            return 9;
        }

        @Override
        public String fieldName1() {
            return "class : ";
        }

        @Override
        public String fieldName2() {
            return "desc : ";
        }
    }

    public static class MethodReferencePoolEntry extends TwoReferencesPoolEntry {

        public MethodReferencePoolEntry(ByteBuffer buffer) {
            super(buffer);
        }

        protected byte getId() {
            return 10;
        }

        @Override
        public String fieldName1() {
            return "class : ";
        }

        @Override
        public String fieldName2() {
            return "desc : ";
        }
    }

    public static class InterfaceMethodReferencePoolEntry extends TwoReferencesPoolEntry {

        public InterfaceMethodReferencePoolEntry(ByteBuffer buffer) {
            super(buffer);
        }

        protected byte getId() {
            return 11;
        }

        @Override
        public String fieldName1() {
            return "class : ";
        }

        @Override
        public String fieldName2() {
            return "desc : ";
        }
    }

    public static class NameAndTypePoolEntry extends TwoReferencesPoolEntry {

        public NameAndTypePoolEntry(ByteBuffer buffer) {
            super(buffer);
        }

        protected byte getId() {
            return 12;
        }

        @Override
        public String fieldName1() {
            return "name : ";
        }

        @Override
        public String fieldName2() {
            return "desc : ";
        }
    }

    public static class MethodHandlePoolEntry extends TwoFieldsPoolEntry<Field.ByteField, Field.ConstantPoolReferenceField> {

        public MethodHandlePoolEntry(ByteBuffer buffer) {
            super(new Field.ByteField(buffer), new Field.ConstantPoolReferenceField(buffer));
        }

        protected byte getId() {
            return 15;
        }

        @Override
        public String fieldName1() {
            return "kind : ";
        }

        @Override
        public String fieldName2() {
            return "ref : ";
        }
    }

    public static class MethodTypePoolEntry extends OneReferencePoolEntry {

        public MethodTypePoolEntry(ByteBuffer buffer) {
            super(buffer);
        }

        protected byte getId() {
            return 16;
        }

        @Override
        protected String fieldName() {
            return "method_type : ";
        }
    }

    public static class InvokeDynamicPoolEntry extends TwoReferencesPoolEntry {

        public InvokeDynamicPoolEntry(ByteBuffer buffer) {
            super(buffer);
        }

        protected byte getId() {
            return 18;
        }

        @Override
        public String fieldName1() {
            return "bootstrap_index : ";
        }

        @Override
        public String fieldName2() {
            return "desc : ";
        }
    }
}
