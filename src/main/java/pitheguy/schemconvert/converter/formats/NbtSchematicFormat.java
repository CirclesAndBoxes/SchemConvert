package pitheguy.schemconvert.converter.formats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import pitheguy.schemconvert.converter.ConversionException;
import pitheguy.schemconvert.converter.Entity;
import pitheguy.schemconvert.converter.Schematic;
import pitheguy.schemconvert.nbt.NbtUtil;
import pitheguy.schemconvert.nbt.tags.CompoundTag;
import pitheguy.schemconvert.nbt.tags.DoubleTag;
import pitheguy.schemconvert.nbt.tags.IntTag;
import pitheguy.schemconvert.nbt.tags.ListTag;
import pitheguy.schemconvert.nbt.tags.StringTag;
import pitheguy.schemconvert.nbt.tags.Tag;

public class NbtSchematicFormat implements SchematicFormat {

    @Override
    public Schematic read(File file) throws IOException {
        CompoundTag tag = NbtUtil.read(file);

        // Read the file
        ListTag sizeTag = tag.getList("size");
        int[] size = new int[3];
        for (int i = 0; i < 3; i++) size[i] = ((IntTag) sizeTag.get(i)).value();

        // Get the palette
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

        ListTag entitiesTag = tag.getList("entities");
        for (Tag value : entitiesTag) {
            CompoundTag entityTag = (CompoundTag) value;
            ListTag posTag = entityTag.getList("pos");
            double[] pos = new double[3];
            for (int i = 0; i < 3; i++) pos[i] = ((DoubleTag) posTag.get(i)).value();
            CompoundTag nbt = entityTag.getCompound("nbt");
            builder.addEntity(nbt.getString("id"), pos[0], pos[1], pos[2], nbt);
        }
        return builder.build();
    }

    //@Override
    public void writeX(File file, Schematic schematic) throws IOException {
        //schematic.getPalette()

        int[] full_sections = {0, 0, 0};
        int[] partial_sections = {0, 0, 0};

        int[] size = schematic.getSize();

        for (int i = 0; i < 3; i++) {
            full_sections[i] = size[i] / 48;

            partial_sections[i] = size[i] % 48;
            if (partial_sections[i] == 0){
                partial_sections[i] = 48;
                full_sections[i] -= 1;
            }
        }

        for (int i = 0; i <= full_sections[0]; i++) {
            for (int j = 0; j <= full_sections[1]; j++) {
                for (int k = 0; k <= full_sections[2]; k++) {
                    int[] newCoords = {48 * i, 48 * j, 48 * k};
                    int[] newSize = {48, 48, 48};

                    if (i == full_sections[0]){
                        newSize[0] = partial_sections[0];
                    }
                    if (j == full_sections[1]){
                        newSize[1] = partial_sections[1];
                    }
                    if (k == full_sections[2]){
                        newSize[2] = partial_sections[2];
                    }

                    Schematic part = schematic.partialSchematic(newCoords, newSize);
                    
                    File tempFile = new File(file.getName().replace(".nbt","_" + i + "_" + j + "_" + k) + ".nbt");

                    //write_by_block(tempFile, part);

                }
            }
        }
    }


    // Old command that will be used for actual implementation
    //@Override
    public void write2(File file, Schematic schematic) throws IOException {

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
                    // if its null
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
        ListTag entitiesTag = new ListTag(Tag.TAG_COMPOUND);
        for (Entity entity : schematic.getEntities()) {
            CompoundTag entityTag = new CompoundTag();
            ListTag posTag = new ListTag(Tag.TAG_DOUBLE);
            posTag.add(new DoubleTag(entity.x()));
            posTag.add(new DoubleTag(entity.y()));
            posTag.add(new DoubleTag(entity.z()));
            entityTag.put("pos", posTag);
            ListTag blockPosTag = new ListTag(Tag.TAG_INT);
            blockPosTag.add(new IntTag((int) entity.x()));
            blockPosTag.add(new IntTag((int) entity.y()));
            blockPosTag.add(new IntTag((int) entity.z()));
            entityTag.put("blockPos", blockPosTag);
            CompoundTag nbt = entity.nbt();
            nbt.put("id", new StringTag(entity.id()));
            entityTag.put("nbt", nbt);
            entitiesTag.add(entityTag);
        }
        tag.put("entities", entitiesTag);
        tag.put("size", sizeTag);
        tag.put("blocks", blocksTag);
        tag.put("palette", paletteTag);
        tag.put("DataVersion", new IntTag(schematic.getDataVersion()));
        NbtUtil.write(tag, file);
    }

    // real name write_by_block
    @Override
    public void write(File file, Schematic schematic) throws IOException {

        int[] size = schematic.getSize();
        // if (size[0] > 48 || size[1] > 48 || size[2] > 48)
        //     throw new ConversionException("The schematic is too large to use this format!");
        

        ArrayList<CompoundTag> list_of_tag = new ArrayList<>();
        ArrayList<ListTag> list_of_blockTag = new ArrayList<>();

        ArrayList<File> list_of_files = new ArrayList<>();

        for (String block_string : schematic.getPalette()) {
            String folderPath = block_string; // Relative path
            // String folderPath = "C:\\Users\\YourUser\\Documents\\newFolder"; // Absolute path example
            // Creates the folder
            list_of_files.add(new File(folderPath));
            if (!list_of_files.getLast().exists()) { // Check if the folder already exists
                boolean created = list_of_files.getLast().mkdir(); // Use mkdir() for a single directory
                // boolean created = newFolder.mkdirs(); // Use mkdirs() for creating parent directories too
                if (created) {
                    System.out.println("Folder '" + folderPath + "' created successfully.");
                } else {
                    System.out.println("Failed to create folder '" + folderPath + "'.");
                }
            } else {
                System.out.println("Folder '" + folderPath + "' already exists.");
            }
            
            list_of_tag.add(new CompoundTag());
            list_of_blockTag.add(new ListTag(Tag.TAG_COMPOUND));
        }

        // // Compound Tag is the tag of all of the other tags
        // CompoundTag tag = new CompoundTag();
        ListTag sizeTag = new ListTag(Tag.TAG_INT);
        for (int i : size) sizeTag.add(new IntTag(i));
        ListTag paletteTag = new ListTag(Tag.TAG_COMPOUND);
        for (String block : schematic.getPalette()) paletteTag.add(NbtUtil.convertFromBlockString(block));
        // ListTag blocksTag = new ListTag(Tag.TAG_COMPOUND);
            
        for (int x = 0; x < size[0]; x++) {
            for (int y = 0; y < size[1]; y++) {
                for (int z = 0; z < size[2]; z++) {

                    int state = schematic.getPaletteBlock(x, y, z);
                    // if its null
                    if (state == -1) continue;
                    
                    // for each state in 

                    ListTag posTag = new ListTag(Tag.TAG_INT);
                    for (int i : new int[]{x, y, z}) posTag.add(new IntTag(i));

                    CompoundTag entry = new CompoundTag();
                    entry.put("pos", posTag);
                    entry.put("state", new IntTag(state));

                    // I'm ignoring actual and block entities now as much as I can right now
                    //if (schematic.hasBlockEntityAt(x, y, z)) entry.put("nbt", schematic.getBlockEntityAt(x, y, z));

                    list_of_blockTag.get(state).add(entry);

                }
            }
        }
        ListTag entitiesTag = new ListTag(Tag.TAG_COMPOUND);
        for (Entity entity : schematic.getEntities()) {
            CompoundTag entityTag = new CompoundTag();
            ListTag posTag = new ListTag(Tag.TAG_DOUBLE);
            posTag.add(new DoubleTag(entity.x()));
            posTag.add(new DoubleTag(entity.y()));
            posTag.add(new DoubleTag(entity.z()));
            entityTag.put("pos", posTag);
            ListTag blockPosTag = new ListTag(Tag.TAG_INT);
            blockPosTag.add(new IntTag((int) entity.x()));
            blockPosTag.add(new IntTag((int) entity.y()));
            blockPosTag.add(new IntTag((int) entity.z()));
            entityTag.put("blockPos", blockPosTag);
            CompoundTag nbt = entity.nbt();
            nbt.put("id", new StringTag(entity.id()));
            entityTag.put("nbt", nbt);
            entitiesTag.add(entityTag);
        }


        for (int i = 0; i < list_of_tag.size(); i ++) {
            list_of_tag.get(i).put("entities", entitiesTag);
            list_of_tag.get(i).put("size", sizeTag);
            list_of_tag.get(i).put("blocks", list_of_blockTag.get(i));
            list_of_tag.get(i).put("palette", paletteTag);
            list_of_tag.get(i).put("DataVersion", new IntTag(schematic.getDataVersion()));
            NbtUtil.write(list_of_tag.get(i), list_of_files.get(i));
        }
        

    }

    @Override
    public String getExtension() {
        return ".nbt";
    }
}
