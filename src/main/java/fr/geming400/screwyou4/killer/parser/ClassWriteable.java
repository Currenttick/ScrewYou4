package fr.geming400.screwyou4.killer.parser;

import java.nio.ByteBuffer;

public interface ClassWriteable {

    void write(ByteBuffer buffer);

    String format(String name, ConstantPool constantPool);

    int size();

}
