package pitheguy.schemconvert.converter;

import pitheguy.schemconvert.converter.formats.*;
import pitheguy.schemconvert.nbt.tags.CompoundTag;
import pitheguy.schemconvert.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Schematic {
    private final String[][][] blocks;
    private final List<String> palette;
    private final Map<Pos, CompoundTag> blockEntities;
    private final List<Entity> entities;
    private final int dataVersion;
    private final File sourceFile;

    private Schematic(String[][][] blocks, List<String> palette, Map<Pos, CompoundTag> blockEntities, List<Entity> entities, int dataVersion, File sourceFile) {
        this.blocks = blocks;
        this.palette = palette;
        this.blockEntities = blockEntities;
        this.entities = entities;
        this.dataVersion = dataVersion;
        this.sourceFile = sourceFile;
    }

    public int[] getSize() {
        return new int[] {blocks.length, blocks[0].length, blocks[0][0].length};
    }

    public String getBlock(int x, int y, int z) {
        return blocks[x][y][z];
    }

    public int getPaletteBlock(int x, int y, int z) {
        String block = getBlock(x, y, z);
        if (block == null) return -1;
        return palette.indexOf(block);
    }

    public List<String> getPalette() {
        return palette;
    }

    public Map<Pos, CompoundTag> getBlockEntities() {
        return blockEntities;
    }

    public boolean hasBlockEntityAt(int x, int y, int z) {
        return blockEntities.containsKey(new Pos(x, y, z));
    }

    public CompoundTag getBlockEntityAt(int x, int y, int z) {
        return blockEntities.get(new Pos(x, y, z));
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void write(File file, SchematicFormat format) throws IOException {
        format.write(file, this);
    }

    public static Schematic read(File file) throws IOException {
        return switch (Util.getExtension(file.getName())) {
            case ".nbt" -> SchematicFormats.NBT.read(file);
            case ".schem" -> SchematicFormats.SCHEM.read(file);
            case ".litematic" -> SchematicFormats.LITEMATIC.read(file);
            default -> throw new IllegalArgumentException("Unsupported format: " + Util.getExtension(file.getName()));
        };
    }

    public int getDataVersion() {
        return dataVersion;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public static class Builder {
        private final String[][][] blocks;
        private final SequencedSet<String> palette;
        private final Map<Pos, CompoundTag> blockEntities;
        private final List<Entity> entities;
        private final File sourceFile;
        private final int dataVersion;

        public Builder(File sourceFile, int dataVersion, int xSize, int ySize, int zSize) {
            this.blocks = new String[xSize][ySize][zSize];
            this.palette = new LinkedHashSet<>();
            this.blockEntities = new HashMap<>();
            this.entities = new ArrayList<>();
            this.sourceFile = sourceFile;
            this.dataVersion = dataVersion;
        }

        public void setBlockAt(int x, int y, int z, String block) {
            this.blocks[x][y][z] = block;
            palette.add(block);
        }

        public void addBlockEntity(int x, int y, int z, CompoundTag entity) {
            this.blockEntities.put(new Pos(x, y, z), entity);
        }

        public void addEntity(String id, double x, double y, double z, CompoundTag nbt) {
            entities.add(new Entity(id, x, y, z, nbt));
        }

        public Schematic build() {
            return new Schematic(blocks, palette.stream().toList(), blockEntities, entities, dataVersion, sourceFile);
        }
    }
}
