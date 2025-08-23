package pitheguy.schemconvert.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class VarIntIterator implements Iterator<Integer> {
    private int index = 0;
    private final byte[] data;

    public VarIntIterator(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean hasNext() {
        return index < data.length;
    }

    @Override
    public Integer next() {
        if (!hasNext()) throw new NoSuchElementException();
        return nextVarInt();
    }

    public int nextVarInt() {
        byte b = data[index++];
        int i = b & 0x7f;
        for (int shift = 7; (b & 0x80) != 0; shift += 7) {
            b = data[index++];
            i |= (b & 0x7f) << shift;
        }
        return i;
    }
}
