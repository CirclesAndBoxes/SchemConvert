package pitheguy.schemconvert.converter.formats;

import pitheguy.schemconvert.Main;
import pitheguy.schemconvert.converter.*;
import pitheguy.schemconvert.nbt.NbtUtil;
import pitheguy.schemconvert.nbt.tags.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class NbtSchematicFormat implements SchematicFormat {

    @Override
    public Schematic read(File file) throws IOException {
        CompoundTag tag = NbtUtil.read(file);
        ListTag sizeTag = tag.getList("size");
        int[] size = new int[3];
        for (int i = 0; i < 3; i++) size[i] = ((IntTag) sizeTag.get(i)).value();
        ListTag paletteTag = tag.getList("palette");
        String[] palette = new String[paletteTag.size()];
        for (int i = 0; i < paletteTag.size(); i++)
            palette[i] = NbtUtil.convertToBlockString((CompoundTag) paletteTag.get(i));
        Schematic.Builder builder = new Schematic.Builder(file, tag.getInt("DataVersion"), size[0], size[1], size[2]);
        ListTag blocksTag = tag.getList("blocks");
        for (Tag value : blocksTag) {
            CompoundTag entry = (CompoundTag) value;
            ListTag posTag = entry.getList("pos");
            int[] pos = new int[3];
            for (int i = 0; i < 3; i++) pos[i] = ((IntTag) posTag.get(i)).value();
            int state = entry.getInt("state");
            builder.setBlockAt(pos[0], pos[1], pos[2], palette[state]);
            if (entry.contains("nbt", Tag.TAG_COMPOUND))
                builder.addBlockEntity(pos[0], pos[1], pos[2], entry.getCompound("nbt"));
        }
        return builder.build();
    }

    @Override
    public void write(File file, Schematic schematic) throws IOException {
        int[] size = schematic.getSize();
        if (size[0] > 48 || size[1] > 48 || size[2] > 48)
            throw new ConversionException("The schematic is too large to use this format!");
        CompoundTag tag = new CompoundTag();
        ListTag sizeTag = new ListTag(Tag.TAG_INT);
        for (int i : size) sizeTag.add(new IntTag(i));
        ListTag paletteTag = new ListTag(Tag.TAG_COMPOUND);
        for (String block : schematic.getPalette()) paletteTag.add(NbtUtil.convertFromBlockString(block));
        ListTag blocksTag = new ListTag(Tag.TAG_COMPOUND);
        for (int x = 0; x < size[0]; x++) {
            for (int y = 0; y < size[1]; y++) {
                for (int z = 0; z < size[2]; z++) {
                    int state = schematic.getPaletteBlock(x, y, z);
                    if (state == -1) continue;
                    ListTag posTag = new ListTag(Tag.TAG_INT);
                    for (int i : new int[]{x, y, z}) posTag.add(new IntTag(i));
                    CompoundTag entry = new CompoundTag();
                    entry.put("pos", posTag);
                    entry.put("state", new IntTag(state));
                    if (schematic.hasBlockEntityAt(x, y, z)) entry.put("nbt", schematic.getBlockEntityAt(x, y, z));
                    blocksTag.add(entry);
                }
            }
        }
        tag.put("size", sizeTag);
        tag.put("blocks", blocksTag);
        tag.put("palette", paletteTag);
        tag.put("DataVersion", new IntTag(schematic.getDataVersion()));
        NbtUtil.write(tag, file, "");
    }

    @Override
    public String getExtension() {
        return ".nbt";
    }
}
