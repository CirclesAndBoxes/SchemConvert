package pitheguy.schemconvert.ui;

import pitheguy.schemconvert.converter.*;
import pitheguy.schemconvert.converter.formats.SchematicFormat;
import pitheguy.schemconvert.nbt.NbtException;
import pitheguy.schemconvert.util.Util;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class Gui extends JFrame {


    private JTextField inputPathField;
    private JTextField outputPathField;
    private FormatSelectionDropdown formatDropdown;
    private JButton convertButton;

    public Gui() {
        super("SchemConvert");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createInputPathPanel());
        panel.add(createOutputPathPanel());
        formatDropdown = new FormatSelectionDropdown();
        formatDropdown.addActionListener(e -> {
            if (!outputPathField.getText().isEmpty()) {
                String outputPath = Util.stripExtension(outputPathField.getText()) + formatDropdown.getSelectedFormat().getExtension();
                outputPathField.setText(outputPath);
            }
        });
        panel.add(formatDropdown);
        panel.add(createButtonPanel());
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createInputPathPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JLabel label = new JLabel("Input File:");
        label.setPreferredSize(new Dimension(70, 20));
        panel.add(label);
        inputPathField = new JTextField(20);
        inputPathField.setEditable(false);
        panel.add(inputPathField);
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> {
            JFileChooser chooser = createFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                inputPathField.setText(selectedFile.getAbsolutePath());
                if (outputPathField.getText().isEmpty()) {
                    String outputPath = Util.stripExtension(selectedFile.getAbsolutePath()) + formatDropdown.getSelectedFormat().getExtension();
                    outputPathField.setText(outputPath);
                }
            }
            updateButtonState();
        });
        panel.add(browseButton);
        return panel;
    }

    private JPanel createOutputPathPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JLabel label = new JLabel("Output File:");
        label.setPreferredSize(new Dimension(70, 20));
        panel.add(label);
        outputPathField = new JTextField(20);
        outputPathField.setEditable(false);
        panel.add(outputPathField);
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> {
            JFileChooser chooser = createFileChooser();
            if (!inputPathField.getText().isEmpty())
                chooser.setCurrentDirectory(new File(inputPathField.getText()).getParentFile());
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                outputPathField.setText(chooser.getSelectedFile().getAbsolutePath());
                String extension = Util.getExtension(outputPathField.getText());
                if (Converter.SCHEMATIC_EXTENSIONS.contains(extension)) formatDropdown.setSelectedFormat(extension);
            }
            updateButtonState();
        });
        panel.add(browseButton);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        convertButton = new JButton("Convert");
        convertButton.setEnabled(false);
        convertButton.addActionListener(e -> convert());
        panel.add(convertButton);
        return panel;
    }

    private void updateButtonState() {
        convertButton.setEnabled(!inputPathField.getText().isEmpty() && !outputPathField.getText().isEmpty());
    }

    private void convert() {
        try {
            File inputFile = new File(inputPathField.getText());
            File outputFile = new File(outputPathField.getText());
            SchematicFormat format = formatDropdown.getSelectedFormat();
            new Converter().convert(inputFile, outputFile, format);
            JOptionPane.showMessageDialog(this, "Schematic successfully converted!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "An error occurred while reading the input file.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NbtException e) {
            JOptionPane.showMessageDialog(this, "An error occurred while parsing the schematic. If you're sure this is a valid schematic, please report this!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (ConversionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private JFileChooser createFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return Converter.SCHEMATIC_EXTENSIONS.stream().anyMatch(ext -> f.getName().toLowerCase().endsWith(ext));
            }

            @Override
            public String getDescription() {
                StringBuilder sb = new StringBuilder();
                sb.append("Schematic files (");
                sb.append(Converter.SCHEMATIC_EXTENSIONS.stream().map(ext -> "*" + ext).collect(Collectors.joining("; ")));
                sb.append(")");
                return sb.toString();
            }
        });
        return chooser;
    }


}
