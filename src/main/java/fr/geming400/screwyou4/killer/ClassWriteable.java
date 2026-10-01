package fr.geming400.screwyou4.killer;

import java.nio.ByteBuffer;

public interface ClassWriteable {

    void encode(ByteBuffer buffer);
    int size();
}
