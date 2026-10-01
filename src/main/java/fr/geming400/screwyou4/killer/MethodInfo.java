package fr.geming400.screwyou4.killer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MethodInfo implements ClassWriteable {

    private short accessFlags;
    private short nameIndex;
    private short descriptorIndex;
    private final Field.AttributeArrayField attributes;
    private final ArrayList<PoolEntry> poolEntries;

    public MethodInfo(ByteBuffer buffer, ArrayList<PoolEntry> poolEntries) {
        this.accessFlags = buffer.getShort();
        this.nameIndex = buffer.getShort();
        this.descriptorIndex = buffer.getShort();
        this.attributes = new Field.AttributeArrayField(buffer, poolEntries);
        this.poolEntries = poolEntries;
    }

    @Override
    public String toString() {
        return "methodInfo{" + this.accessFlags + ", " + this.poolEntries.get(this.nameIndex).format(this.poolEntries) + ", " + this.poolEntries.get(this.descriptorIndex).format(this.poolEntries) + ", " + this.attributes + "}";
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.putShort(this.accessFlags);
        buffer.putShort(this.nameIndex);
        buffer.putShort(this.descriptorIndex);
        this.attributes.encode(buffer);
    }

    @Override
    public int size() {
        return 6 + this.attributes.size();
    }
}
