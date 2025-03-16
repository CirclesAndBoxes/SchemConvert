package pitheguy.schemconvert.converter.formats;

import pitheguy.schemconvert.converter.Schematic;
import pitheguy.schemconvert.nbt.NbtUtil;
import pitheguy.schemconvert.nbt.tags.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SchemSchematicFormat implements SchematicFormat {
    @Override
    public Schematic read(File file) throws IOException {
        CompoundTag tag = NbtUtil.read(file);
        if (tag.contains("Schematic", Tag.TAG_COMPOUND)) return readV3(file, tag.getCompound("Schematic"));
        else return readV2(file, tag);
    }

    private Schematic readV3(File file, CompoundTag schematicTag) {
        int xSize = schematicTag.getShort("Width");
        int ySize = schematicTag.getShort("Height");
        int zSize = schematicTag.getShort("Length");
        CompoundTag blocksTag = schematicTag.getCompound("Blocks");
        CompoundTag paletteTag = blocksTag.getCompound("Palette");
        int paletteMax = paletteTag.keySet().size() - 1;
        String[] palette = new String[paletteMax + 1];
        for (String key : paletteTag.keySet()) {
            int index = paletteTag.getInt(key);
            palette[index] = key;
        }
        Schematic.Builder builder = new Schematic.Builder(file, schematicTag.getInt("DataVersion"), xSize, ySize, zSize);
        byte[] blockData = blocksTag.getByteArray("Data");
        int index = 0;
        for (int y = 0; y < ySize; y++)
            for (int z = 0; z < zSize; z++)
                for (int x = 0; x < xSize; x++)
                    builder.setBlockAt(x, y, z, palette[blockData[index++]]);
        ListTag blockEntitiesTag = blocksTag.getList("BlockEntities");
        for (Tag value : blockEntitiesTag) {
            CompoundTag blockEntity = (CompoundTag) value;
            int[] pos = blockEntity.getIntArray("Pos");
            builder.addBlockEntity(pos[0], pos[1], pos[2], blockEntity);
        }
        return builder.build();
    }

    private Schematic readV2(File file, CompoundTag schematicTag) {
        int paletteMax = schematicTag.getInt("PaletteMax");
        String[] palette = new String[paletteMax];
        CompoundTag paletteTag = schematicTag.getCompound("Palette");
        for (String key : paletteTag.keySet()) {
            int index = paletteTag.getInt(key);
            palette[index] = key;
        }
        int xSize = schematicTag.getShort("Width");
        int ySize = schematicTag.getShort("Height");
        int zSize = schematicTag.getShort("Length");
        Schematic.Builder builder = new Schematic.Builder(file, schematicTag.getInt("DataVersion"), xSize, ySize, zSize);
        byte[] blockData = schematicTag.getByteArray("BlockData");
        int index = 0;
        for (int y = 0; y < ySize; y++)
            for (int z = 0; z < zSize; z++)
                for (int x = 0; x < xSize; x++)
                    builder.setBlockAt(x, y, z, palette[blockData[index++]]);
        ListTag blockEntitiesTag = schematicTag.getList("BlockEntities");
        for (Tag value : blockEntitiesTag) {
            CompoundTag blockEntity = (CompoundTag) value;
            int[] pos = blockEntity.getIntArray("Pos");
            builder.addBlockEntity(pos[0], pos[1], pos[2], blockEntity);
        }
        return builder.build();
    }

    @Override
    public void write(File file, Schematic schematic) throws IOException {
        CompoundTag tag = new CompoundTag();
        int[] size = schematic.getSize();
        tag.put("Version", new IntTag(2));
        tag.put("Width", new ShortTag((short) size[0]));
        tag.put("Height", new ShortTag((short) size[1]));
        tag.put("Length", new ShortTag((short) size[2]));
        tag.put("PaletteMax", new IntTag(schematic.getPalette().size()));
        List<String> palette = new ArrayList<>(schematic.getPalette());
        byte[] blockData = new byte[size[0] * size[1] * size[2]];
        int index = 0;
        for (int y = 0; y < size[1]; y++) {
            for (int z = 0; z < size[2]; z++) {
                for (int x = 0; x < size[0]; x++) {
                    int blockIndex = schematic.getPaletteBlock(x, y, z);
                    if (blockIndex == -1) {
                        if (!palette.contains("minecraft:air")) palette.add("minecraft:air");
                        blockIndex = palette.indexOf("minecraft:air");
                    }
                    blockData[index++] = (byte) blockIndex;
                }
            }
        }
        tag.put("BlockData", new ByteArrayTag(blockData));
        CompoundTag paletteTag = new CompoundTag();
        for (int i = 0; i < palette.size(); i++) paletteTag.put(palette.get(i), new IntTag(i));
        tag.put("Palette", paletteTag);
        ListTag blockEntitiesTag = new ListTag(Tag.TAG_COMPOUND);
        schematic.getBlockEntities().forEach((pos, entity) -> {
            if (!entity.contains("Pos", Tag.TAG_INT_ARRAY))
                entity.put("Pos", new IntArrayTag(new int[]{pos.x(), pos.y(), pos.z()}));
        });
        tag.put("BlockEntities", blockEntitiesTag);
        tag.put("DataVersion", new IntTag(schematic.getDataVersion()));
        NbtUtil.write(tag, file, "Schematic");
    }
}
