package fr.geming400.screwyou4.killer.parser;

import java.nio.ByteBuffer;

public abstract class Infos implements ClassWriteable {

    public static class FieldInfo extends Field.FieldElement4<Field.ShortField, Field.ConstantPoolReferenceField, Field.ConstantPoolReferenceField, Field.ShortPrefixedArrayField<Attribute>> {

        private final ConstantPool constantPool;

        public FieldInfo(ByteBuffer buffer, ConstantPool constantPool) {
            super(
                    buffer,
                    Field.ShortField::new,
                    Field.ConstantPoolReferenceField::new,
                    Field.ConstantPoolReferenceField::new,
                    b -> new Field.ShortPrefixedArrayField<>(b, (ByteBuffer buffer1) -> new Attribute(buffer1, constantPool))
            );
            this.constantPool = constantPool;
        }

        @Override
        public String fieldName4() {
            return "attribs : ";
        }

        @Override
        public String fieldName3() {
            return "desc : ";
        }

        @Override
        public String fieldName2() {
            return "name : ";
        }

        @Override
        public String fieldName1() {
            return "acc_flags : ";
        }

        public String getName() {
            return this.field2.<PoolEntry.Utf8PoolEntry>getExpected(this.constantPool).fieldName();
        }
    }

    public static class MethodInfo extends Field.FieldElement4<Field.ShortField, Field.ConstantPoolReferenceField, Field.ConstantPoolReferenceField, Field.ShortPrefixedArrayField<Attribute>> {

        private final ConstantPool constantPool;

        public MethodInfo(ByteBuffer buffer, ConstantPool constantPool) {
            super(
                    buffer,
                    Field.ShortField::new,
                    Field.ConstantPoolReferenceField::new,
                    Field.ConstantPoolReferenceField::new,
                    b -> new Field.ShortPrefixedArrayField<>(b, (ByteBuffer buffer1) -> new Attribute(buffer1, constantPool))
            );
            this.constantPool = constantPool;
        }

        @Override
        public String fieldName4() {
            return "attribs : ";
        }

        @Override
        public String fieldName3() {
            return "desc : ";
        }

        @Override
        public String fieldName2() {
            return "name : ";
        }

        @Override
        public String fieldName1() {
            return "acc_flags : ";
        }

        public String getName() {
            return this.field2.<PoolEntry.Utf8PoolEntry>getExpected(this.constantPool).field.getValue() + this.field3.<PoolEntry.Utf8PoolEntry>getExpected(this.constantPool).field.getValue();
        }
    }
}
