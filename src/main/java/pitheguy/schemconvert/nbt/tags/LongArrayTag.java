package pitheguy.schemconvert.nbt.tags;

import java.io.*;

public record LongArrayTag(long[] values) implements Tag {
    @Override
    public void writeContents(DataOutputStream out) throws IOException {
        out.writeInt(values.length);
        for (long value : values) out.writeLong(value);
    }

    @Override
    public byte getType() {
        return 12;
    }

    public static LongArrayTag readContents(DataInputStream in) throws IOException {
        int size = in.readInt();
        long[] values = new long[size];
        for (int i = 0; i < size; ++i) values[i] = in.readLong();
        return new LongArrayTag(values);
    }
}
