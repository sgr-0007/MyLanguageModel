import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;

public class BiGramLM extends JFrame {

    public BiGramLM() {
        readFile();
    }

    private void readFile() {
        MyHashTable head = new MyHashTable(257);
        Pattern regex = Pattern.compile("[^.'a-z]\\s");

        try (BufferedReader reader = new BufferedReader(new FileReader("news.txt"))) {
            String line;
            String previous = null;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");

                //first line bi gram - null case
                if (previous == null && words.length > 1) {
                    head.setBigramWord(words[0], words[1]);
                    previous = words[0];

                }
                for (int i = 1; i < words.length; i++) {
                    head.setBigramWord(previous, words[i]);
                    previous = words[i];
                }
                //handling last word of each line
                previous = words[words.length - 1];

            }
            System.out.println(head.size());
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

            this.setTitle("Bi-Gram");
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
        labelUniqueSum.setText("Total Unique Bi-Grams: " + sumUnique);
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

