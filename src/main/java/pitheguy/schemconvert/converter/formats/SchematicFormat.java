package pitheguy.schemconvert.converter.formats;

import pitheguy.schemconvert.converter.Schematic;

import java.io.File;
import java.io.IOException;

public interface SchematicFormat {
    Schematic read(File file) throws IOException;

    void write(File file, Schematic schematic) throws IOException;

    String getExtension();
}
