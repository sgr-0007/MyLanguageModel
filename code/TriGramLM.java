import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;

public class TriGramLM extends JFrame {

    public TriGramLM() {
        readFile();

    }


    private void readFile() {
        MyHashTable head = new MyHashTable(257);
        Pattern regex = Pattern.compile("[^.'a-z]\\s");

        try (BufferedReader reader = new BufferedReader(new FileReader("news.txt"))) {
            String line;
            String previous1 = null;
            String previous2 = null;

            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");

                // Handle first line and subsequent lines with fewer than 3 words
                if (words.length >= 3) {
                    // First line trigram - null cases
                    if (previous2 == null) {
                        head.setTrigramWord(words[0], words[1], words[2]);
                        previous2 = words[1];
                        previous1 = words[0];
                    } else {
                        // Subsequent trigrams
                        head.setTrigramWord(previous1, previous2, words[0]);
                    }

                    for (int i = 1; i < words.length - 1; i++) {
                        head.setTrigramWord(previous2, words[i], words[i + 1]);
                        previous2 = words[i];
                    }
                }

                // Handle last two words only if the line has enough words
                if (words.length >= 2) {
                    previous2 = words[words.length - 2];
                    previous1 = words[words.length - 1];
                }
            }

            VocabularyListResultWrapper list = head.fetchVocabularyList();
            System.out.println(head.size());

            String[] columnNames = {"Words", "Count"};
            // word and count column read-only mode
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    // word and count column read-only mode
                    return false;
                }
            };

            List<VocabularyObject> processedObj = new ArrayList<>();
            for (VocabularyObject vocabObj : list.getVocabObjects()) {
                Matcher matcher = regex.matcher(vocabObj.getWords());
                if (!matcher.find()) {
                    processedObj.add(vocabObj);
                    String[] rowData = {String.join(", ", vocabObj.getWords()), String.valueOf(vocabObj.getCount())};
                    tableModel.addRow(rowData);
                }
            }
            //retains only processed vocabulary objects (only lower case, full stop and an apostrophe)
            list.getVocabObjects().retainAll(processedObj);

            int sumUnique = list.getVocabObjects().size();

            int average = sumUnique / head.size();

            int standardDeviation = (int) list.getStandardDeviation();
            JTable table = new JTable(tableModel);


            JPanel labelPanel = getLabelPanel(sumUnique, average, standardDeviation);
            JPanel buttonPanel = new JPanel();
            this.setLayout(new BorderLayout());
            this.add(buttonPanel, BorderLayout.NORTH);
            this.add(new JScrollPane(table), BorderLayout.CENTER);
            this.add(labelPanel, BorderLayout.SOUTH);

            this.setTitle("Tri-Gram");
            this.pack();
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.setSize(800, 500);
            this.setVisible(true);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage());
        }
    }

    // label components of statistics
    private static JPanel getLabelPanel(int sumUnique, int average, int sd) {
        JLabel labelUniqueSum = new JLabel();
        JLabel labelAverage = new JLabel();
        JLabel labelStandardDeviation = new JLabel();
        labelUniqueSum.setText("Total Unique Tri-Grams: " + sumUnique);
        labelAverage.setText("Average: " + average);
        labelStandardDeviation.setText("Standard Deviation :" + sd);
        labelUniqueSum.setFont(new Font("Arial", Font.BOLD, 14));
        labelAverage.setFont(new Font("Arial", Font.BOLD, 14));
        labelStandardDeviation.setFont(new Font("Arial", Font.BOLD, 14));
        labelUniqueSum.setForeground(Color.RED);
        labelAverage.setForeground(Color.DARK_GRAY);
        labelStandardDeviation.setForeground(Color.ORANGE);
        labelUniqueSum.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        labelAverage.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        labelStandardDeviation.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JPanel labelPanel = new JPanel();
        labelPanel.add(labelUniqueSum);
        labelPanel.add(labelAverage);
        labelPanel.add(labelStandardDeviation);
        return labelPanel;
    }


}
