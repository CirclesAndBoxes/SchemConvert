package pitheguy.schemconvert.converter.formats;

import pitheguy.schemconvert.converter.*;
import pitheguy.schemconvert.nbt.NbtUtil;
import pitheguy.schemconvert.nbt.tags.*;
import pitheguy.schemconvert.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.BitSet;
import java.util.List;

public class LitematicSchematicFormat implements SchematicFormat {
    @Override
    public Schematic read(File file) throws IOException {
        CompoundTag tag = NbtUtil.read(file);
        CompoundTag regions = tag.getCompound("Regions");
        if (regions.keySet().size() > 1) throw new ConversionException("Multi-region litematic files are not supported");
        CompoundTag region = regions.getCompound(regions.keySet().iterator().next());
        ListTag paletteTag = region.getList("BlockStatePalette");
        CompoundTag sizeTag = region.getCompound("Size");
        int[] size = new int[] {Math.abs(sizeTag.getInt("x")), Math.abs(sizeTag.getInt("y")), Math.abs(sizeTag.getInt("z"))};
        CompoundTag regionPosTag = region.getCompound("Position");
        int regionX = regionPosTag.getInt("x");
        int regionY = regionPosTag.getInt("y");
        int regionZ = regionPosTag.getInt("z");
        String[] palette = new String[paletteTag.size()];
        for (int i = 0; i < paletteTag.size(); i++) palette[i] = NbtUtil.convertToBlockString((CompoundTag) paletteTag.get(i));
        Schematic.Builder builder = new Schematic.Builder(file, tag.getInt("MinecraftDataVersion"), size);
        int[] blockStates = unpackBlockStates(region.getLongArray("BlockStates"), size, palette);
        int index = 0;
        for (int y = 0; y < size[1]; y++)
            for (int z = 0; z < size[2]; z++)
                for (int x = 0; x < size[0]; x++)
                    builder.setBlockAt(x, y, z, palette[blockStates[index++]]);
        ListTag tileEntitiesTag = region.getList("TileEntities");
        for (Tag value : tileEntitiesTag) {
            CompoundTag entityTag = (CompoundTag) value;
            builder.addBlockEntity(entityTag.getInt("x"), entityTag.getInt("y"), entityTag.getInt("z"), entityTag);
            entityTag.remove("x");
            entityTag.remove("y");
            entityTag.remove("z");
        }
        ListTag entitiesTag = region.getList("Entities");
        for (Tag value : entitiesTag) {
            CompoundTag entityTag = (CompoundTag) value;
            ListTag posTag = entityTag.getList("Pos");
            double[] pos = new double[3];
            for (int i = 0; i < 3; i++) pos[i] = ((DoubleTag) posTag.get(i)).value();
            builder.addEntity(entityTag.getString("id"), pos[0] + regionX, pos[1] + regionY, pos[2] + regionZ, entityTag);
        }
        return builder.build();
    }

    private int[] unpackBlockStates(long[] blockStates, int[] size, String[] palette) {
        BlockStateContainer container = BlockStateContainer.fromLongArray(blockStates, size, palette.length);
        return container.getBlockStates();
    }

    @Override
    public void write(File file, Schematic schematic) throws IOException {
        CompoundTag tag = new CompoundTag();
        CompoundTag regions = new CompoundTag();
        CompoundTag region = new CompoundTag();
        ListTag paletteTag = new ListTag(Tag.TAG_COMPOUND);
        List<String> palette = schematic.getPalette();
        for (String entry : palette) paletteTag.add(NbtUtil.convertFromBlockString(entry));
        region.put("BlockStatePalette", paletteTag);
        int[] size = schematic.getSize();
        CompoundTag sizeTag = new CompoundTag();
        sizeTag.put("x", new IntTag(size[0]));
        sizeTag.put("y", new IntTag(size[1]));
        sizeTag.put("z", new IntTag(size[2]));
        region.put("Size", sizeTag);
        int[] blockStates = new int[size[0] * size[1] * size[2]];
        int index = 0;
        for (int y = 0; y < size[1]; y++)
            for (int z = 0; z < size[2]; z++)
                for (int x = 0; x < size[0]; x++)
                    blockStates[index++] = schematic.getPaletteBlock(x, y, z) + 1;
        LongArrayTag blockStatesTag = new LongArrayTag(packBlockStates(blockStates, palette.toArray(new String[0])));
        region.put("BlockStates", blockStatesTag);
        CompoundTag posTag = new CompoundTag();
        posTag.put("x", new IntTag(0));
        posTag.put("y", new IntTag(0));
        posTag.put("z", new IntTag(0));
        region.put("Position", posTag);
        ListTag tileEntitiesTag = new ListTag(Tag.TAG_COMPOUND);
        schematic.getBlockEntities().forEach((pos, entity) -> {
            entity.put("x", new IntTag(pos.x()));
            entity.put("y", new IntTag(pos.y()));
            entity.put("z", new IntTag(pos.z()));
            tileEntitiesTag.add(entity);
        });
        region.put("TileEntities", tileEntitiesTag);
        ListTag entitiesTag = new ListTag(Tag.TAG_COMPOUND);
        for (Entity entity : schematic.getEntities()) {
            CompoundTag entityTag = entity.nbt();
            ListTag entityPosTag = new ListTag(Tag.TAG_DOUBLE);
            entityPosTag.add(new DoubleTag(entity.x()));
            entityPosTag.add(new DoubleTag(entity.y()));
            entityPosTag.add(new DoubleTag(entity.z()));
            entityTag.put("Pos", entityPosTag);
            entitiesTag.add(entityTag);
        }
        region.put("Entities", entitiesTag);
        regions.put(Util.stripExtension(schematic.getSourceFile().getName()), region);
        tag.put("Regions", regions);
        tag.put("MinecraftDataVersion", new IntTag(schematic.getDataVersion()));
        tag.put("Version", new IntTag(6));
        CompoundTag metadataTag = new CompoundTag();
        metadataTag.put("EnclosingSize", sizeTag);
        metadataTag.put("Name", new StringTag(schematic.getSourceFile().getName()));
        metadataTag.put("TimeCreated", new LongTag(getCreationTime(schematic.getSourceFile())));
        metadataTag.put("TimeModified", new LongTag(schematic.getSourceFile().lastModified()));
        metadataTag.put("TotalVolume", new IntTag(size[0] * size[1] * size[2]));
        metadataTag.put("RegionCount", new IntTag(1));
        tag.put("Metadata", metadataTag);
        NbtUtil.write(tag, file);
    }

    private static long getCreationTime(File file) throws IOException {
        return Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime().toMillis();
    }

    private long[] packBlockStates(int[] states,String[] palette) {
        BlockStateContainer container = new BlockStateContainer(palette.length);
        for (int state : states) container.addBlockState(state);
        return container.toLongArray();
    }

    @Override
    public String getExtension() {
        return ".litematic";
    }

    private static class BlockStateContainer {
        private final BitSet bits;
        private final int bitsPerValue;
        private int numBits;

        public BlockStateContainer(int paletteSize) {
            this(new BitSet(), Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(paletteSize)), 0);
        }

        private BlockStateContainer(BitSet bits, int bitsPerValue, int numBits) {
            this.bits = bits;
            this.bitsPerValue = bitsPerValue;
            this.numBits = numBits;
        }

        public void addBlockState(int blockState) {
            for (int i = 0; i < bitsPerValue; i++) {
                if ((blockState & 1 << i) != 0) bits.set(numBits + i);
                else bits.clear(numBits + i);
            }
            numBits += bitsPerValue;
        }

        public int[] getBlockStates() {
            int totalValues = numBits / bitsPerValue;
            int[] blockStates = new int[totalValues];
            for (int i = 0; i < totalValues; i++) {
                int value = 0;
                for (int j = 0; j < bitsPerValue; j++) if (bits.get(i * bitsPerValue + j)) value |= 1 << j;
                blockStates[i] = value;
            }
            return blockStates;
        }

        public long[] toLongArray() {
            return bits.toLongArray();
        }

        public static BlockStateContainer fromLongArray(long[] longs, int[] size, int paletteSize) {
            int bitsPerValue = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(paletteSize));
            int totalValues = size[0] * size[1] * size[2];
            int totalBits = totalValues * bitsPerValue;

            BitSet bits = BitSet.valueOf(longs);
            if (totalBits % Long.SIZE != 0) {
                int startIndex = (longs.length - 1) * Long.SIZE;
                int remainingBits = totalBits - startIndex;
                long value = longs[longs.length - 1];
                for (int i = 0; i < remainingBits; i++) {
                    if ((value & (1L << i)) != 0) bits.set(startIndex + i);
                    else bits.clear(startIndex + i);
                }
            }
            return new BlockStateContainer(bits, bitsPerValue, totalBits);
        }
    }
}
