package pitheguy.schemconvert.nbt.tags;

import java.io.*;

public record StringTag(String value) implements Tag {
    @Override
    public void writeContents(DataOutputStream out) throws IOException {
        out.writeUTF(value);
    }

    @Override
    public byte getType() {
        return 8;
    }

    public static StringTag readContents(DataInputStream in) throws IOException {
        return new StringTag(in.readUTF());
    }
}
