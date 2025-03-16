package pitheguy.schemconvert.nbt.tags;

import java.io.*;

public record ByteArrayTag(byte[] values) implements Tag {
    @Override
    public void writeContents(DataOutputStream out) throws IOException {
        out.writeInt(values.length);
        out.write(values);
    }

    @Override
    public byte getType() {
        return 7;
    }

    public static ByteArrayTag readContents(DataInputStream in) throws IOException {
        int size = in.readInt();
        byte[] values = new byte[size];
        in.readFully(values);
        return new ByteArrayTag(values);
    }
}
