package pitheguy.schemconvert.nbt.tags;

import java.io.*;

public record IntArrayTag(int[] values) implements Tag {
    @Override
    public void writeContents(DataOutputStream out) throws IOException {
        out.writeInt(values.length);
        for (int value : values) out.writeInt(value);
    }

    @Override
    public byte getType() {
        return 11;
    }

    public static IntArrayTag readContents(DataInputStream in) throws IOException {
        int size = in.readInt();
        int[] values = new int[size];
        for (int i = 0; i < size; ++i) values[i] = in.readInt();
        return new IntArrayTag(values);
    }
}
