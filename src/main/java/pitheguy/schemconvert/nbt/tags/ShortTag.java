package pitheguy.schemconvert.nbt.tags;

import java.io.*;

public record ShortTag(short value) implements Tag {
    @Override
    public void writeContents(DataOutputStream out) throws IOException {
        out.writeShort(value);
    }

    @Override
    public byte getType() {
        return 2;
    }

    public static ShortTag readContents(DataInputStream in) throws IOException {
        return new ShortTag(in.readShort());
    }
}
