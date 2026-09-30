package fr.geming400.screwyou4.killer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AttributeInfo {

    private short nameIndex;
    private final ArrayList<Byte> info;
    private final ArrayList<PoolEntry> poolEntries;

    public AttributeInfo(short nameIndex, ArrayList<Byte> info, ArrayList<PoolEntry> poolEntries) {
        this.nameIndex = nameIndex;
        this.info = new ArrayList<>(info);
        this.poolEntries = poolEntries;
    }

    public AttributeInfo(ByteBuffer buffer, ArrayList<PoolEntry> poolEntries) {
        this.nameIndex = buffer.getShort();
        int attributesCount = buffer.getInt();
        this.info = new ArrayList<>(attributesCount);
        for (int i = 0; i < attributesCount; i++) {
            this.info.add(buffer.get());
        }
        this.poolEntries = poolEntries;
    }

    public short getNameIndex() {
        return this.nameIndex;
    }

    public void setNameIndex(short nameIndex) {
        this.nameIndex = nameIndex;
    }

    public ArrayList<Byte> getInfo() {
        return this.info;
    }

    @Override
    public String toString() {
        return "attributeInfo{" + this.poolEntries.get(this.nameIndex).format(this.poolEntries) + ", " + this.info + "}";
    }
}
