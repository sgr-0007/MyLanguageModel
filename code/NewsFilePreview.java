import java.awt.*;
import java.io.*;
import javax.swing.*;

public class NewsFilePreview extends JFrame {
    JTextArea textArea;

    public NewsFilePreview() {
        // Create text area component
        textArea = new JTextArea(20, 40);

        // Add text area component to the frame
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Set properties of the frame
        setTitle("News Preview");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);

        // Preview news.txt
        previewFile();
    }

    private void previewFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("news.txt"))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            textArea.setText(content.toString());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage());
        }
    }

}
