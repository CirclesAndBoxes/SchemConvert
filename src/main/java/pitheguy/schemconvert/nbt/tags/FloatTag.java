package pitheguy.schemconvert.nbt.tags;

import java.io.*;

public record FloatTag(float value) implements Tag {
    @Override
    public void writeContents(DataOutputStream out) throws IOException {
        out.writeFloat(value);
    }

    @Override
    public byte getType() {
        return 5;
    }

    public static FloatTag readContents(DataInputStream in) throws IOException {
        return new FloatTag(in.readFloat());
    }
}
