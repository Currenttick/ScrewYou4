package fr.geming400.screwyou4.killer.parser;

import java.nio.ByteBuffer;

public class Attribute extends Field.FieldElement2<Field.ConstantPoolReferenceField, Field.IntPrefixedArrayField<Field.ByteField>> {

    private final ConstantPool constantPool;

    public Attribute(ByteBuffer buffer, ConstantPool constantPool) {
        super(
                buffer,
                Field.ConstantPoolReferenceField::new,
                b -> new Field.IntPrefixedArrayField<>(b, Field.ByteField::new)
        );
        this.constantPool = constantPool;
    }

    @Override
    public String fieldName2() {
        return "";
    }

    @Override
    public String fieldName1() {
        return "";
    }

    public String getName() {
        return this.constantPool.<PoolEntry.Utf8PoolEntry>getExpected(this.field1.getValue()).getField().getValue();
    }
}