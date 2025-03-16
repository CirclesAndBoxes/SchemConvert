package pitheguy.schemconvert.nbt.tags;

import java.io.*;

public record ByteTag(byte value) implements Tag {
    public void writeContents(DataOutputStream out) throws IOException {
        out.writeByte(value);
    }

    @Override
    public byte getType() {
        return 1;
    }

    public static ByteTag readContents(DataInputStream in) throws IOException {
        return new ByteTag((in.readByte()));
    }
}
