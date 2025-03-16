package pitheguy.schemconvert.nbt.tags;

import java.io.*;

public record LongTag(long value) implements Tag {
    @Override
    public void writeContents(DataOutputStream out) throws IOException {
        out.writeLong(value);
    }

    @Override
    public byte getType() {
        return 4;
    }

    public static LongTag readContents(DataInputStream in) throws IOException {
        return new LongTag(in.readLong());
    }
}
