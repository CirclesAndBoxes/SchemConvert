package pitheguy.schemconvert;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import pitheguy.schemconvert.converter.ConversionException;
import pitheguy.schemconvert.converter.Converter;
import pitheguy.schemconvert.converter.formats.SchematicFormat;
import pitheguy.schemconvert.converter.formats.SchematicFormats;
import pitheguy.schemconvert.ui.Gui;

import java.io.*;

public class Main {
    public static boolean isCommandLine = false;

    public static void main(String[] args) {
        if (args.length == 0) new Gui();
        else {
            isCommandLine = true;
            OptionParser parser = new OptionParser();
            parser.accepts("help", "Show this help message").forHelp();
            parser.accepts("input", "Input file").withRequiredArg().ofType(File.class).required();
            parser.accepts("output", "Output file").withRequiredArg().ofType(File.class).required();
            parser.accepts("format", "Output format").withRequiredArg().ofType(String.class).required();
            OptionSet options = parser.parse(args);
            File inputFile = (File) options.valueOf("input");
            File outputFile = (File) options.valueOf("output");
            String formatStr = (String) options.valueOf("format");
            SchematicFormat format = switch (formatStr) {
                case "nbt" -> SchematicFormats.NBT;
                case "schem" -> SchematicFormats.SCHEM;
                case "litematic" -> SchematicFormats.LITEMATIC;
                default -> {
                    System.err.println("Unrecognized output format: " + formatStr);
                    System.exit(1);
                    yield null; // Required by compiler
                }
            };
            if (Converter.SCHEMATIC_EXTENSIONS.stream().noneMatch(ext -> inputFile.getName().endsWith(ext)))
                System.err.println("Unrecognized input file: " + inputFile);
            try {
                new Converter().convert(inputFile, outputFile, format);
                System.out.println("Successfully converted " + inputFile + " to " + outputFile);
            } catch (ConversionException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            } catch (Exception e) {
                System.err.println("An error occurred while converting " + inputFile + " to " + outputFile);
                System.exit(1);
            }
        }
    }
}