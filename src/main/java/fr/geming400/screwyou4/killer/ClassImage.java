package fr.geming400.screwyou4.killer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static fr.geming400.screwyou4.killer.ClassJudger.log;

public class ClassImage {

    private final ByteBuffer data;
    private final short minorVersion;
    private final short majorVersion;
    private final ArrayList<PoolEntry> poolEntries;
    private final short accessFlags;
    private final short thisClass;
    private final short superClass;
    private final ArrayList<Short> interfaces;
    private final ArrayList<FieldInfo> fields;
    private final ArrayList<MethodInfo> methods;
    private final ArrayList<AttributeInfo> attributes;

    public ClassImage(byte[] data) {
        this.data = ByteBuffer.wrap(data);
        if (this.data.getInt() != 0xCAFEBABE) {
            throw new IllegalArgumentException("Magic constant not found :(");
        }
        this.minorVersion = this.data.getShort();
        log("version minor : " + this.minorVersion);
        this.majorVersion = this.data.getShort();
        log("version major : " + this.majorVersion);
        short constantPoolCount = (short) (this.data.getShort() - 1);
        this.poolEntries = new ArrayList<>(constantPoolCount + 1);
        this.poolEntries.add(PoolEntry.TYPES.get((byte) 0).apply(this.data));
        log("constant pool entries size: " + constantPoolCount);
        for (int i = 0; i < constantPoolCount; i++) {
            byte tag = this.data.get();
            this.poolEntries.add(PoolEntry.TYPES.get(tag).apply(this.data));
            if (tag == 5 || tag == 6) {
                this.poolEntries.add(PoolEntry.TYPES.get((byte) 0).apply(this.data));
                i++;
            }
        }
        StringBuilder sb = new StringBuilder("{");
        for (PoolEntry poolEntry : this.poolEntries) {
            sb.append(poolEntry.format(this.poolEntries))
                    .append(", ");
        }
        sb.delete(sb.length() - 2, sb.length()).append("}");
        log(sb.toString());
        this.accessFlags = this.data.getShort();
        log("access flags : " + this.accessFlags);
        this.thisClass = this.data.getShort();
        log("this class : " + this.poolEntries.get(this.thisClass).format(this.poolEntries));
        this.superClass = this.data.getShort();
        log("super class : " + this.poolEntries.get(this.superClass).format(this.poolEntries));
        short interfaceCount = this.data.getShort();
        this.interfaces = new ArrayList<>(interfaceCount);
        for (int i = 0; i < interfaceCount; i++) {
            this.interfaces.add(this.data.getShort());
        }
        log("interfaces size : " + this.interfaces.size() + this.interfaces.stream().map(i -> this.poolEntries.get(i).format(this.poolEntries)).toList());
        short fieldCount = this.data.getShort();
        this.fields = new ArrayList<>(fieldCount);
        for (int i = 0; i < fieldCount; i++) {
            this.fields.add(new FieldInfo(this.data, this.poolEntries));
        }
        log("fields size : " + this.fields.size() + this.fields);
        short methodCount = this.data.getShort();
        this.methods = new ArrayList<>(methodCount);
        for (int i = 0; i < methodCount; i++) {
            this.methods.add(new MethodInfo(this.data, this.poolEntries));
        }
        log("methods size : " + this.methods.size() + this.methods);
        short attributeCount = this.data.getShort();
        this.attributes = new ArrayList<>(attributeCount);
        for (int i = 0; i < attributeCount; i++) {
            this.attributes.add(new AttributeInfo(this.data, this.poolEntries));
        }
        log("attributes size : " + this.attributes.size() + this.attributes);
    }
}
