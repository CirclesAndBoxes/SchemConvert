package pitheguy.schemconvert.nbt.tags;

import java.io.*;

public record DoubleTag(double value) implements Tag {
    @Override
    public void writeContents(DataOutputStream out) throws IOException {
        out.writeDouble(value);
    }

    @Override
    public byte getType() {
        return 6;
    }

    public static DoubleTag readContents(DataInputStream in) throws IOException {
        return new DoubleTag(in.readDouble());
    }
}
