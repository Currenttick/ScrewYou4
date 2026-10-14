package fr.geming400.screwyou4.killer.parser;

import java.nio.ByteBuffer;

public class ConstantPool {

    private final Field.ConstantPoolEntriesArray entries;

    public ConstantPool(ByteBuffer buffer) {
        this.entries = new Field.ConstantPoolEntriesArray(buffer);
    }

    @Override
    public String toString() {
        return this.entries.format("", this);
    }

    public PoolEntry get(int index) {
        return this.entries.get(index);
    }

    public <P extends PoolEntry> P getExpected(int index) {
        return (P) this.entries.get(index);
    }
}
