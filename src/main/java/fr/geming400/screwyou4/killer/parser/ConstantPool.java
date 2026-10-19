package fr.geming400.screwyou4.killer.parser;

import java.nio.ByteBuffer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConstantPool implements ClassWriteable {

    private final Field.ConstantPoolEntriesArray entries;
    public final ClassRepresentation classRepresentation;

    public ConstantPool(ByteBuffer buffer, ClassRepresentation classRepresentation) {
        this.entries = new Field.ConstantPoolEntriesArray(buffer);
        this.classRepresentation = classRepresentation;
    }

    @Override
    public String toString() {
        return this.entries.format("", this);
    }

    public PoolEntry get(int index) {
        return this.entries.get(index);
    }

    public PoolEntry get(Predicate<PoolEntry> predicate, Supplier<PoolEntry> defaultEntry) {
        return this.entries.value.stream().filter(predicate).findFirst().orElseGet(() -> {
            PoolEntry entry = defaultEntry.get();
            this.entries.append(entry);
            return entry;
        });
    }

    public short getIndex(Predicate<PoolEntry> predicate, Supplier<PoolEntry> defaultEntry) {
        return (short) this.entries.value.indexOf(this.get(predicate, defaultEntry));
    }

    public void append(PoolEntry entry) {
        this.entries.append(entry);
    }

    public <P extends PoolEntry> P getExpected(int index) {
        // noinspection unchecked
        return (P) this.entries.get(index);
    }

    @Override
    public void write(ByteBuffer buffer) {
        this.entries.write(buffer);
    }

    @Override
    public String format(String name, ConstantPool constantPool) {
        return "";
    }

    @Override
    public int size() {
        return this.entries.size();
    }
}
