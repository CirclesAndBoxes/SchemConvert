package pitheguy.schemconvert.nbt.tags;

import java.io.*;

public record IntTag(int value) implements Tag {
    @Override
    public void writeContents(DataOutputStream out) throws IOException {
        out.writeInt(value);
    }

    @Override
    public byte getType() {
        return 3;
    }

    public static IntTag readContents(DataInputStream in) throws IOException {
        return new IntTag(in.readInt());
    }
}