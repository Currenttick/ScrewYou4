package fr.geming400.screwyou4.killer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class FieldInfo {

    private short accessFlags;
    private short nameIndex;
    private short descriptorIndex;
    private final ArrayList<AttributeInfo> attributes;
    private final ArrayList<PoolEntry> poolEntries;

    public FieldInfo(short accessFlags, short nameIndex, short descriptorIndex, ArrayList<AttributeInfo> attributes, ArrayList<PoolEntry> poolEntries) {
        this.accessFlags = accessFlags;
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
        this.attributes = new ArrayList<>(attributes);
        this.poolEntries = poolEntries;
    }

    public FieldInfo(ByteBuffer buffer, ArrayList<PoolEntry> poolEntries) {
        this.accessFlags = buffer.getShort();
        this.nameIndex = buffer.getShort();
        this.descriptorIndex = buffer.getShort();
        short attributesCount = buffer.getShort();
        this.attributes = new ArrayList<>(attributesCount);
        for (int i = 0; i < attributesCount; i++) {
            this.attributes.add(new AttributeInfo(buffer, poolEntries));
        }
        this.poolEntries = poolEntries;
    }

    public short getAccessFlags() {
        return this.accessFlags;
    }

    public void setAccessFlags(short accessFlags) {
        this.accessFlags = accessFlags;
    }

    public short getNameIndex() {
        return this.nameIndex;
    }

    public void setNameIndex(short nameIndex) {
        this.nameIndex = nameIndex;
    }

    public short getDescriptorIndex() {
        return this.descriptorIndex;
    }

    public void setDescriptorIndex(short descriptorIndex) {
        this.descriptorIndex = descriptorIndex;
    }

    public ArrayList<AttributeInfo> getAttributes() {
        return this.attributes;
    }

    @Override
    public String toString() {
        return "fieldInfo{" + this.accessFlags + ", " + this.poolEntries.get(this.nameIndex).format(this.poolEntries) + ", " + this.poolEntries.get(this.descriptorIndex).format(this.poolEntries) + ", " + this.attributes + "}";
    }
}
