package pitheguy.schemconvert.ui;

import pitheguy.schemconvert.converter.Converter;
import pitheguy.schemconvert.converter.formats.SchematicFormat;
import pitheguy.schemconvert.converter.formats.SchematicFormats;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class FormatSelectionDropdown extends JPanel {
    private JComboBox<String> comboBox;

    public FormatSelectionDropdown() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        JLabel label = new JLabel("Convert To: ");
        label.setPreferredSize(new Dimension(70, 20));
        add(label);
        comboBox = new JComboBox<>(Converter.SCHEMATIC_EXTENSIONS.toArray(new String[0]));
        add(comboBox);
    }

    public SchematicFormat getSelectedFormat() {
        return switch (comboBox.getSelectedItem().toString()) {
            case ".nbt" -> SchematicFormats.NBT;
            case ".schem" -> SchematicFormats.SCHEM;
            case ".litematic" -> SchematicFormats.LITEMATIC;
            case ".bp" -> SchematicFormats.AXIOM;
            default -> throw new IllegalStateException("Unexpected value: " + comboBox.getSelectedItem());
        };
    }

    public void addActionListener(ActionListener l) {
        comboBox.addActionListener(l);
    }

    public void setSelectedFormat(String extension) {
        comboBox.setSelectedItem(extension);
    }
}
