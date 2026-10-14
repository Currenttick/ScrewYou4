package fr.geming400.screwyou4.killer.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static fr.geming400.screwyou4.killer.ClassJudger.log;

public class ClassRepresentation {

    private final ByteBuffer data;
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


    public ClassRepresentation(byte[] data) {
        this.data = ByteBuffer.wrap(data);
        if (this.data.getInt() != 0xCAFEBABE) {
            throw new IllegalArgumentException("Magic constant not found :(");
        }
        this.minorVersion = new Field.ShortField(this.data);
        log("version minor : " + this.minorVersion.value);
        this.majorVersion = new Field.ShortField(this.data);
        log("version major : " + this.majorVersion.value);
        this.constantPool = new ConstantPool(this.data);
        log(this.constantPool.toString());
        this.accessFlags = new Field.ShortField(this.data);
        log("access flags : " + this.accessFlags.value);
        this.thisClass = new Field.ConstantPoolReferenceField(this.data);
        log(this.thisClass.format("this class : ", this.constantPool));
        this.superClass = new Field.ConstantPoolReferenceField(this.data);
        log(this.superClass.format("super class : ", this.constantPool));
        this.interfaces = new Field.ShortPrefixedArrayField<>(this.data, Field.ConstantPoolReferenceField::new);
        log(this.interfaces.format("interfaces size : ", this.constantPool));
        this.fields = new Field.ShortPrefixedArrayField<>(this.data, b -> new Infos.FieldInfo(b, this.constantPool));
        log(this.fields.format("fields size : ", this.constantPool));
        this.methods = new Field.ShortPrefixedArrayField<>(this.data, b -> new Infos.MethodInfo(b, this.constantPool));
        log(this.methods.format("methods size : ", this.constantPool));
        this.attributes = new Field.ShortPrefixedArrayField<>(this.data, (ByteBuffer buffer) -> new Attribute(buffer, this.constantPool));
        log(this.attributes.format("attributes size : ", this.constantPool));
        log("========== SUCCESS!! ==========");
    }

    public ArrayList<Infos.MethodInfo> getMethodInfos() {
        return this.methods.value;
    }
}
