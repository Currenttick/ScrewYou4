package fr.geming400.screwyou4.killer.parser;

import java.nio.ByteBuffer;

public interface ClassWriteable {

    void write(ByteBuffer buffer);

    String format(String name, ConstantPool constantPool);

    int size();

    default ByteBuffer toBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(this.size());
        this.write(buffer);

        return buffer.flip();
    }

    static ByteBuffer toBuffer(ClassWriteable classWriteable) {
        ByteBuffer buffer = ByteBuffer.allocate(classWriteable.size());
        classWriteable.write(buffer);

        return buffer.flip();
    }
}
