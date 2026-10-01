package fr.geming400.screwyou4.killer;

import fr.geming400.screwyou4.Utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public abstract class PoolEntry {

    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final Map<Byte, Function<ByteBuffer, PoolEntry>> TYPES = Map.ofEntries(
            Map.entry((byte) 0x00, DummyPoolEntry::new),
            Map.entry((byte) 0x01, Utf8PoolEntry::new),
            Map.entry((byte) 0x03, IntPoolEntry::new),
            Map.entry((byte) 0x04, FloatPoolEntry::new),
            Map.entry((byte) 0x05, LongPoolEntry::new),
            Map.entry((byte) 0x06, DoublePoolEntry::new),
            Map.entry((byte) 0x07, ClassReferencePoolEntry::new),
            Map.entry((byte) 0x08, StringReferencePoolEntry::new),
            Map.entry((byte) 0x09, FieldReferencePoolEntry::new),
            Map.entry((byte) 0x0A, MethodReferencePoolEntry::new),
            Map.entry((byte) 0x0B, InterfaceMethodReferencePoolEntry::new),
            Map.entry((byte) 0x0C, NameAndTypeDescriptorPoolEntry::new),
            Map.entry((byte) 0x0F, MethodHandlePoolEntry::new),
            Map.entry((byte) 0x10, MethodTypePoolEntry::new),
            Map.entry((byte) 0x11, DynamicPoolEntry::new),
            Map.entry((byte) 0x12, InvokeDynamicPoolEntry::new),
            Map.entry((byte) 0x13, ModulePoolEntry::new),
            Map.entry((byte) 0x14, PackagePoolEntry::new)
    );

    protected final Map<String, Field<?>> fields;
    private PoolEntry(ByteBuffer ignoredBuffer, Map<String, Field<?>> fields) {
        this.fields = fields;
    }

    public void encode(ByteBuffer buffer) {
        for (Field<?> field : this.fields.values()) {
            field.encode(buffer);
        }
    }
    
    public final String getName() {
        return this.getClass().getSimpleName().replace("PoolEntry", "");
    }

    @Override
    public boolean equals(Object obj) {
        if (this.getClass().isInstance(obj)) {
            return this.fields.equals(this.getClass().cast(obj).fields);
        }
        return false;
    }

    public String format(ArrayList<PoolEntry> poolEntries) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getName())
                .append("{");
        for (Field<?> entry : this.fields.values()) {
            sb.append(entry)
                    .append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getName())
                .append("{");
        for (Map.Entry<String, Field<?>> entry : this.fields.entrySet()) {
            sb.append(entry.getKey())
                    .append(" : ")
                    .append(entry.getValue())
                    .append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("}");
        return sb.toString();
    }

    public static class DummyPoolEntry extends PoolEntry {
        private DummyPoolEntry(ByteBuffer buffer) {
            super(buffer, Map.of());
        }

        @Override
        public String format(ArrayList<PoolEntry> poolEntries) {
            return this.getName() + "{}";
        }
    }

    public static class Utf8PoolEntry extends PoolEntry {
        private Utf8PoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "utf8", new Field.Utf8Field(buffer)
            ));
        }
    }

    public static class IntPoolEntry extends PoolEntry {
        private IntPoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "int", new Field.IntField(buffer)
            ));
        }
    }

    public static class FloatPoolEntry extends PoolEntry {
        private FloatPoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "float", new Field.FloatField(buffer)
            ));
        }
    }

    public static class LongPoolEntry extends PoolEntry {
        private LongPoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "long", new Field.LongField(buffer)
            ));
        }
    }

    public static class DoublePoolEntry extends PoolEntry {
        private DoublePoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "double", new Field.DoubleField(buffer)
            ));
        }
    }

    public static class ClassReferencePoolEntry extends PoolEntry {
        private ClassReferencePoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "index", new Field.ShortField(buffer)
            ));
        }

        @Override
        public String format(ArrayList<PoolEntry> poolEntries) {
            return this.getName() +
                    "{" +
                    poolEntries.get(this.fields.get("index").getValue().intValue()).format(poolEntries) +
                    "}";
        }
    }

    public static class StringReferencePoolEntry extends PoolEntry {
        private StringReferencePoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "index", new Field.ShortField(buffer)
            ));
        }

        @Override
        public String format(ArrayList<PoolEntry> poolEntries) {
            return this.getName() +
                    "{" +
                    poolEntries.get(this.fields.get("index").getValue().intValue()).format(poolEntries) +
                    "}";
        }
    }

    public static class FieldReferencePoolEntry extends PoolEntry {
        private FieldReferencePoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "classIndex", new Field.ShortField(buffer),
                    "NTdescIndex", new Field.ShortField(buffer)
            ));
        }

        @Override
        public String format(ArrayList<PoolEntry> poolEntries) {
            return this.getName() +
                    "{" +
                    poolEntries.get(this.fields.get("classIndex").getValue().intValue()).format(poolEntries) +
                    ", " +
                    poolEntries.get(this.fields.get("NTdescIndex").getValue().intValue()).format(poolEntries) +
                    "}";
        }
    }

    public static class MethodReferencePoolEntry extends PoolEntry {
        private MethodReferencePoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "classIndex", new Field.ShortField(buffer),
                    "NTdescIndex", new Field.ShortField(buffer)
            ));
        }

        @Override
        public String format(ArrayList<PoolEntry> poolEntries) {
            return this.getName() +
                    "{" +
                    poolEntries.get(this.fields.get("classIndex").getValue().intValue()).format(poolEntries) +
                    ", " +
                    poolEntries.get(this.fields.get("NTdescIndex").getValue().intValue()).format(poolEntries) +
                    "}";
        }
    }

    public static class InterfaceMethodReferencePoolEntry extends PoolEntry {
        private InterfaceMethodReferencePoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "classIndex", new Field.ShortField(buffer),
                    "NTdescIndex", new Field.ShortField(buffer)
            ));
        }

        @Override
        public String format(ArrayList<PoolEntry> poolEntries) {
            return this.getName() +
                    "{" +
                    poolEntries.get(this.fields.get("classIndex").getValue().intValue()).format(poolEntries) +
                    ", " +
                    poolEntries.get(this.fields.get("NTdescIndex").getValue().intValue()).format(poolEntries) +
                    "}";
        }
    }

    public static class NameAndTypeDescriptorPoolEntry extends PoolEntry {
        private NameAndTypeDescriptorPoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "name", new Field.ShortField(buffer),
                    "descriptor", new Field.ShortField(buffer)
            ));
        }

        @Override
        public String format(ArrayList<PoolEntry> poolEntries) {
            return this.getName() +
                    "{" +
                    poolEntries.get(this.fields.get("name").getValue().intValue()).format(poolEntries) +
                    ", " +
                    poolEntries.get(this.fields.get("descriptor").getValue().intValue()).format(poolEntries) +
                    "}";
        }
    }

    public static class MethodHandlePoolEntry extends PoolEntry {
        private MethodHandlePoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "descriptor", new Field.ByteField(buffer),
                    "index", new Field.ShortField(buffer)
            ));
        }

        @Override
        public String format(ArrayList<PoolEntry> poolEntries) {
            return this.getName() +
                    "{" +
                    this.fields.get("descriptor") +
                    ", " +
                    poolEntries.get(this.fields.get("index").getValue().intValue()).format(poolEntries) +
                    "}";
        }
    }

    public static class MethodTypePoolEntry extends PoolEntry {
        private MethodTypePoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "index", new Field.ShortField(buffer)
            ));
        }

        @Override
        public String format(ArrayList<PoolEntry> poolEntries) {
            return this.getName() +
                    "{" +
                    poolEntries.get(this.fields.get("index").getValue().intValue()).format(poolEntries) +
                    "}";
        }
    }

    public static class DynamicPoolEntry extends PoolEntry {
        private DynamicPoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "bootstrapIndex", new Field.ShortField(buffer),
                    "NTdescIndex", new Field.ShortField(buffer)
            ));
        }

        @Override
        public String format(ArrayList<PoolEntry> poolEntries) {
            return this.getName() +
                    "{" +
                    this.fields.get("bootstrapIndex") +
                    ", " +
                    poolEntries.get(this.fields.get("NTdescIndex").getValue().intValue()).format(poolEntries) +
                    "}";
        }
    }

    public static class InvokeDynamicPoolEntry extends PoolEntry {
        private InvokeDynamicPoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "bootstrapIndex", new Field.ShortField(buffer),
                    "NTdescIndex", new Field.ShortField(buffer)
            ));
        }

        @Override
        public String format(ArrayList<PoolEntry> poolEntries) {
            return this.getName() +
                    "{" +
                    this.fields.get("bootstrapIndex") +
                    ", " +
                    poolEntries.get(this.fields.get("NTdescIndex").getValue().intValue()).format(poolEntries) +
                    "}";
        }
    }

    public static class ModulePoolEntry extends PoolEntry {
        private ModulePoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "index", new Field.ShortField(buffer)
            ));
        }

        @Override
        public String format(ArrayList<PoolEntry> poolEntries) {
            return this.getName() +
                    "{" +
                    poolEntries.get(this.fields.get("index").getValue().intValue()).format(poolEntries) +
                    "}";
        }
    }

    public static class PackagePoolEntry extends PoolEntry {
        private PackagePoolEntry(ByteBuffer buffer) {
            super(buffer, Utils.linkedMapOf(
                    "index", new Field.ShortField(buffer)
            ));
        }

        @Override
        public String format(ArrayList<PoolEntry> poolEntries) {
            return this.getName() +
                    "{" +
                    poolEntries.get(this.fields.get("index").getValue().intValue()).format(poolEntries) +
                    "}";
        }
    }
}
