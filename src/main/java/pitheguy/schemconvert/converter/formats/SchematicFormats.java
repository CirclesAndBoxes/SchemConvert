package pitheguy.schemconvert.converter.formats;

public class SchematicFormats {
    public static final SchematicFormat NBT = new NbtSchematicFormat();
    public static final SchemSchematicFormat SCHEM = new SchemSchematicFormat();
    public static final LitematicSchematicFormat LITEMATIC = new LitematicSchematicFormat();

    public static SchematicFormat formatFromExtension(String extension) {
        return switch (extension) {
            case ".nbt" -> NBT;
            case ".schem" -> SCHEM;
            case ".litem" -> LITEMATIC;
            default -> throw new IllegalArgumentException("Unknown extension: " + extension);
        };
    }
}
