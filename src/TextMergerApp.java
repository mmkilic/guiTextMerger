import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TextMergerApp extends JFrame {

    private static final long serialVersionUID = 6160781397779652462L;
	private final JTextArea textArea;
    private final List<File> selectedFiles;
    private File lastDirectory;

    public TextMergerApp() {
        setTitle("Multi File Merger");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        selectedFiles = new ArrayList<>();
        textArea = new JTextArea();
        textArea.setEditable(false);

        JButton selectButton = new JButton("Select");
        JButton mergeButton = new JButton("Merge");

        selectButton.addActionListener(e -> selectFiles());
        mergeButton.addActionListener(e -> mergeFiles());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(selectButton);
        buttonPanel.add(mergeButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    private void selectFiles() {
        JFileChooser chooser = new JFileChooser();

        if (lastDirectory != null) {
            chooser.setCurrentDirectory(lastDirectory);
        }

        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] files = chooser.getSelectedFiles();
        if (files.length == 0) {
            return;
        }

        lastDirectory = chooser.getCurrentDirectory();

        for (File file : files) {
            selectedFiles.add(file);
            textArea.append(file.getAbsolutePath());
            textArea.append(System.lineSeparator());
        }
    }

    private void mergeFiles() {
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No files selected.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File outputFile = new File(
                System.getProperty("user.dir"),
                "SingleFile_" + timestamp + ".txt"
        );

        try (BufferedWriter writer = Files.newBufferedWriter(
                outputFile.toPath(),
                StandardCharsets.UTF_8
        )) {
            for (File file : selectedFiles) {
                writer.write(file.getName());
                writer.newLine();

                Files.lines(file.toPath(), StandardCharsets.UTF_8)
                        .forEach(line -> {
                            try {
                                writer.write(line);
                                writer.newLine();
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });

                writer.newLine();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Merged file created:\n" + outputFile.getAbsolutePath(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            selectedFiles.clear();
            textArea.setText("");

        } catch (IOException | UncheckedIOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error during merge:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TextMergerApp().setVisible(true));
    }
}
