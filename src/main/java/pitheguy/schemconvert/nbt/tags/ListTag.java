package pitheguy.schemconvert.nbt.tags;

import pitheguy.schemconvert.nbt.NbtException;
import pitheguy.schemconvert.nbt.NbtUtil;

import java.io.*;
import java.util.ArrayList;

public class ListTag extends ArrayList<Tag> implements Tag {
    byte type;

    public ListTag(byte type) {
        this.type = type;
    }

    public boolean add(Tag tag) {
        if (tag.getType() != type)
            throw new NbtException("Type mismatch: Attempted to insert tag of type " + tag.getType() + " into list of type " + type);
        return super.add(tag);
    }

    @Override
    public void writeContents(DataOutputStream out) throws IOException {
        out.writeByte(type);
        out.writeInt(size());
        for (Tag tag : this) tag.writeContents(out);
    }

    public static ListTag readContents(DataInputStream in) throws IOException {
        byte type = in.readByte();
        int size = in.readInt();
        ListTag tag = new ListTag(type);
        for (int i = 0; i < size; i++) tag.add(NbtUtil.readByType(type, in));
        return tag;
    }

    @Override
    public byte getType() {
        return 9;
    }
}
