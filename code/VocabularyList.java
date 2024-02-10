import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VocabularyList extends JFrame {

    private DefaultTableModel tableModel;

    public VocabularyList() {

        readFile();
    }

    private void readFile() {
        MyHashTable head = new MyHashTable(257);
        Pattern regex = Pattern.compile("[^.'a-z]");

        try (BufferedReader reader = new BufferedReader(new FileReader("news.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (String word : words) {
                    head.setWord(word);
                }
            }
            System.out.println(head.size());
            VocabularyListResultWrapper list = head.fetchVocabularyList();
            System.out.println(head.size());

            String[] columnNames = {"Words", "Count"};
            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    // word and count column read-only mode
                    return false;
                }
            };

            StringBuilder results = new StringBuilder();
            List<VocabularyObject> processedObj = new ArrayList<>();
            for (VocabularyObject vocabObj : list.getVocabObjects()) {
                Matcher matcher = regex.matcher(vocabObj.getWords());
                if (matcher.find()) {

                    results.append("Mis processed item :").append(vocabObj.getWords()).append("\n");
                } else {
                    processedObj.add(vocabObj);
                    String[] rowData = {String.join(", ", vocabObj.getWords()), String.valueOf(vocabObj.getCount())};
                    tableModel.addRow(rowData);
                }
            }
            //retains only processed vocabulary objects (only lower case, full stop and an apostrophe)
            list.getVocabObjects().retainAll(processedObj);
            JOptionPane.showMessageDialog(null, results.toString(), "Validation Result", JOptionPane.INFORMATION_MESSAGE);

            int sumAll = list.getVocabObjects().stream()
                    .mapToInt(VocabularyObject::getCount)
                    .sum();
            int sumUnique = list.getVocabObjects().size();

            int average = sumUnique / head.size();

            int standardDeviation = (int) list.getStandardDeviation();
            JTable table = new JTable(tableModel);


            JPanel labelPanel = getLabelPanel(sumAll, sumUnique, average, standardDeviation);
            JPanel buttonPanel = new JPanel();
            JButton sortCount = new JButton("Sort Count");
            sortCount.addActionListener(e -> sortCount(list.getVocabObjects()));
            buttonPanel.add(sortCount);

            this.setLayout(new BorderLayout());

            this.add(buttonPanel, BorderLayout.NORTH);
            this.add(new JScrollPane(table), BorderLayout.CENTER);
            this.add(labelPanel, BorderLayout.SOUTH);

            this.setTitle("News Vocabulary");
            this.add(new JScrollPane(table));
            this.pack();
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.setSize(800, 500);
            this.setVisible(true);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage());
        }
    }

    // label components of statistics
    private static JPanel getLabelPanel(int sumAll, int sumUnique, int average, int sd) {
        JLabel labelSumAll = new JLabel();
        JLabel labelUniqueSum = new JLabel();
        JLabel labelAverage = new JLabel();
        JLabel labelStandardDeviation = new JLabel();
        labelSumAll.setText("Total Words : " + sumAll);
        labelUniqueSum.setText("Total Unique: " + sumUnique);
        labelAverage.setText("Average: " + average);
        labelStandardDeviation.setText("Standard Deviation :" + sd);
        labelSumAll.setFont(new Font("Arial", Font.BOLD, 14));
        labelUniqueSum.setFont(new Font("Arial", Font.BOLD, 14));
        labelAverage.setFont(new Font("Arial", Font.BOLD, 14));
        labelStandardDeviation.setFont(new Font("Arial", Font.BOLD, 14));
        labelSumAll.setForeground(Color.BLUE);
        labelUniqueSum.setForeground(Color.RED);
        labelAverage.setForeground(Color.DARK_GRAY);
        labelStandardDeviation.setForeground(Color.ORANGE);
        labelSumAll.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Top, left, bottom, right padding
        labelUniqueSum.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        labelAverage.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        labelStandardDeviation.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));


        JPanel labelPanel = new JPanel();
        labelPanel.add(labelSumAll);
        labelPanel.add(labelUniqueSum);
        labelPanel.add(labelAverage);
        labelPanel.add(labelStandardDeviation);
        return labelPanel;
    }

    private void sortCount(List<VocabularyObject> list) {
        list.sort(Comparator.comparing(VocabularyObject::getCount).reversed());
        updateTable(list);

    }

    private void updateTable(List<VocabularyObject> list) {
        tableModel.setRowCount(0);
        for (VocabularyObject vocabObject : list) {
            String[] rowData = {String.join(", ", vocabObject.getWords()), String.valueOf(vocabObject.getCount())};
            tableModel.addRow(rowData);
        }
    }



}
