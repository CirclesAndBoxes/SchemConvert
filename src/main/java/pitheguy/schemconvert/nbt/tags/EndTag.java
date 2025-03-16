package pitheguy.schemconvert.nbt.tags;

import java.io.DataOutputStream;
import java.io.IOException;

public class EndTag implements Tag {
    public static final EndTag INSTANCE = new EndTag();

    @Override
    public void writeContents(DataOutputStream out) throws IOException {

    }

    @Override
    public byte getType() {
        return 0;
    }
}
