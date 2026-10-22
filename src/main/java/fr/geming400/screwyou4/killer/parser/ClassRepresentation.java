package fr.geming400.screwyou4.killer.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static fr.geming400.screwyou4.killer.ClassJudger.log;

public class ClassRepresentation implements ClassWriteable {

    private final Field.ShortField minorVersion;
    private final Field.ShortField majorVersion;
    private final ConstantPool constantPool;
    private final Field.ShortField accessFlags;
    private final Field.ConstantPoolReferenceField thisClass;
    private final Field.ConstantPoolReferenceField superClass;
    private final Field.ShortPrefixedArrayField<Field.ConstantPoolReferenceField> interfaces;
    private final Field.ShortPrefixedArrayField<Infos.FieldInfo> fields;
    private final Field.ShortPrefixedArrayField<Infos.MethodInfo> methods;
    private final Field.ShortPrefixedArrayField<Attribute> attributes;


    public ClassRepresentation(byte[] dataBuffer) {
        ByteBuffer data = ByteBuffer.wrap(dataBuffer);
        if (data.getInt() != 0xCAFEBABE) {
            throw new IllegalArgumentException("Magic constant not found :(");
        }
        this.minorVersion = new Field.ShortField(data);
        log("version minor : " + this.minorVersion.value);

        this.majorVersion = new Field.ShortField(data);
        log("version major : " + this.majorVersion.value);

        this.constantPool = new ConstantPool(data, this);
        log(this.constantPool.toString());

        this.accessFlags = new Field.ShortField(data);
        log("access flags : " + this.accessFlags.value);

        this.thisClass = new Field.ConstantPoolReferenceField(data);
        log(this.thisClass.format("this class : ", this.constantPool));

        this.superClass = new Field.ConstantPoolReferenceField(data);
        log(this.superClass.format("super class : ", this.constantPool));

        this.interfaces = new Field.ShortPrefixedArrayField<>(data, Field.ConstantPoolReferenceField::new);
        log(this.interfaces.format("interfaces size : ", this.constantPool));

        this.fields = new Field.ShortPrefixedArrayField<>(data, b -> new Infos.FieldInfo(b, this.constantPool));
        log(this.fields.format("fields size : ", this.constantPool));

        this.methods = new Field.ShortPrefixedArrayField<>(data, b -> new Infos.MethodInfo(b, this.constantPool));
        log(this.methods.format("methods size : ", this.constantPool));

        this.attributes = new Field.ShortPrefixedArrayField<>(data, (ByteBuffer buffer) -> new Attribute(buffer, this.constantPool));
        log(this.attributes.format("attributes size : ", this.constantPool));
        log("========== SUCCESS!! ==========");
    }

    public ArrayList<Infos.MethodInfo> getMethodInfos() {
        return this.methods.value;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(0xCAFEBABE);
        buffer.putShort(this.minorVersion.value);
        buffer.putShort(this.majorVersion.value);
        this.constantPool.write(buffer);
        buffer.putShort(this.accessFlags.value);
        buffer.putShort(this.thisClass.value);
        buffer.putShort(this.superClass.value);
        this.interfaces.write(buffer);
        this.fields.write(buffer);
        this.methods.write(buffer);
        this.attributes.write(buffer);
    }

    @Override
    public String format(String name, ConstantPool constantPool) {
        return "";
    }

    @Override
    public int size() {
        return 14 + this.constantPool.size() + this.interfaces.size() + this.fields.size() + this.methods.size() + this.attributes.size();
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(this.size());
        this.write(buffer);
        return buffer.array();
    }
}
