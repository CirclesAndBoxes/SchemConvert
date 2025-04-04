package pitheguy.schemconvert.converter;

import pitheguy.schemconvert.converter.formats.SchematicFormat;
import pitheguy.schemconvert.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Converter {
    public static final List<String> SCHEMATIC_EXTENSIONS = List.of(".nbt", ".schem", ".litematic", ".bp");

    public void convert(File input, File output, SchematicFormat outputFormat) throws IOException, ConversionException {
        Schematic schematic = Schematic.read(input);
        schematic.write(output, outputFormat);
    }

    public void convert(File[] inputs, File outputDir, SchematicFormat outputFormat) throws IOException, ConversionException {
        if (!outputDir.isDirectory()) throw new IOException("Output directory is not a directory!");
        for (File file : inputs) {
            Schematic schematic = Schematic.read(file);
            File outputFile = new File(outputDir, Util.stripExtension(file.getName()) + outputFormat.getExtension());
            schematic.write(outputFile, outputFormat);
        }
    }
}
