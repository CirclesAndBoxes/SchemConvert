package pitheguy.schemconvert.converter.formats;

import pitheguy.schemconvert.converter.*;
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
        else {
            int version = tag.getInt("Version");
            if (version == 2) return readV2(file, tag);
            else if (version == 1) throw new ConversionException("Sponge version 1 is not currently supported.");
            else throw new ConversionException("Unknown sponge version");
        }
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
        if (schematicTag.contains("Entities", Tag.TAG_LIST)) {
            ListTag entitiesTag = schematicTag.getList("Entities");
            for (Tag value : entitiesTag) {
                CompoundTag entity = (CompoundTag) value;
                ListTag posTag = entity.getList("Pos");
                double[] pos = new double[3];
                for (int i = 0; i < 3; i++) pos[i] = ((DoubleTag) posTag.get(i)).value();
                String id = entity.getString("Id");
                CompoundTag nbt = entity.getCompound("Data");
                builder.addEntity(id, pos[0], pos[1], pos[2], nbt);
            }
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
        CompoundTag schematicTag = new CompoundTag();
        int[] size = schematic.getSize();
        schematicTag.put("Version", new IntTag(3));
        schematicTag.put("Width", new ShortTag((short) size[0]));
        schematicTag.put("Height", new ShortTag((short) size[1]));
        schematicTag.put("Length", new ShortTag((short) size[2]));
        CompoundTag blocksTag = new CompoundTag();
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
        blocksTag.put("Data", new ByteArrayTag(blockData));
        CompoundTag paletteTag = new CompoundTag();
        for (int i = 0; i < palette.size(); i++) paletteTag.put(palette.get(i), new IntTag(i));
        blocksTag.put("Palette", paletteTag);
        ListTag blockEntitiesTag = new ListTag(Tag.TAG_COMPOUND);
        schematic.getBlockEntities().forEach((pos, entity) -> {
            if (!entity.contains("Pos", Tag.TAG_INT_ARRAY))
                entity.put("Pos", new IntArrayTag(new int[]{pos.x(), pos.y(), pos.z()}));
        });
        blocksTag.put("BlockEntities", blockEntitiesTag);
        ListTag entitiesTag = new ListTag(Tag.TAG_COMPOUND);
        for (Entity entity : schematic.getEntities()) {
            CompoundTag entityTag = new CompoundTag();
            ListTag posTag = new ListTag(Tag.TAG_DOUBLE);
            posTag.add(new DoubleTag(entity.x()));
            posTag.add(new DoubleTag(entity.y()));
            posTag.add(new DoubleTag(entity.z()));
            entityTag.put("Pos", posTag);
            entityTag.put("Id", new StringTag(entity.id()));
            entityTag.put("Data", entity.nbt());
            entitiesTag.add(entityTag);
        }
        schematicTag.put("Blocks", blocksTag);
        schematicTag.put("Entities", entitiesTag);
        schematicTag.put("DataVersion", new IntTag(schematic.getDataVersion()));
        CompoundTag tag = new CompoundTag();
        tag.put("Schematic", schematicTag);
        NbtUtil.write(tag, file);
    }

    @Override
    public String getExtension() {
        return ".schem";
    }
}
