package fr.geming400.screwyou4.killer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AttributeInfo implements ClassWriteable {

    private short nameIndex;
    private final Field.BigByteArrayField info;
    private final ArrayList<PoolEntry> poolEntries;

    public AttributeInfo(ByteBuffer buffer, ArrayList<PoolEntry> poolEntries) {
        this.nameIndex = buffer.getShort();
        this.info = new Field.BigByteArrayField(buffer);
        this.poolEntries = poolEntries;
    }

    @Override
    public String toString() {
        return "attributeInfo{" + this.poolEntries.get(this.nameIndex).format(this.poolEntries) + ", " + this.info + "}";
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.putShort(this.nameIndex);
        this.info.encode(buffer);
    }

    @Override
    public int size() {
        return 2 + this.info.size();
    }
}
