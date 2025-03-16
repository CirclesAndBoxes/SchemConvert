package pitheguy.schemconvert.converter;

import pitheguy.schemconvert.converter.formats.SchematicFormat;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Converter {
    public static final List<String> SCHEMATIC_EXTENSIONS = List.of(".nbt", ".schem", ".litematic");

    public void convert(File input, File output, SchematicFormat outputFormat) throws IOException, ConversionException {
        Schematic schematic = Schematic.read(input);
        schematic.write(output, outputFormat);
    }
}
