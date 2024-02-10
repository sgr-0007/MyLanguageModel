import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.io.IOException;


public class NGramPrediction extends JFrame {
    JTextField inputTextField;
    JLabel mostLikelyWordsLabel;

    VocabularyListResultWrapper uniGramList;
    VocabularyListResultWrapper biGramList;

    VocabularyListResultWrapper triGramList;


    public NGramPrediction() {

        readFile();
    }

    private void readFile() {
        uniGramList = readUniGram();
        biGramList = readBiGram();
        triGramList = readTriGram();
        JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JPanel inputPanel = new JPanel();
        inputTextField = new JTextField(20);
        JButton biGramBtn = new JButton("UseBigram");
        JButton triGramBtn = new JButton("UseTrigram");

        biGramBtn.addActionListener(e -> uniGramBiGram());
        triGramBtn.addActionListener(e -> biGramTriGram());

        inputPanel.add(inputTextField);
        inputPanel.add(biGramBtn);
        inputPanel.add(triGramBtn);

        mostLikelyWordsLabel = new JLabel("Most Likely Following Words: ");
        mostLikelyWordsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        mainPanel.add(inputPanel);
        mainPanel.add(mostLikelyWordsLabel);
        mainPanel.setBorder(null);

        this.add(mainPanel);

        this.setTitle("MyPredictionLM");
        this.pack();
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(1500, 300);
        this.setVisible(true);
    }

    private void uniGramBiGram() {
        String inputText = inputTextField.getText();
        List<VocabularyObject> mostLikelyFollowingWords = getMostLikelyNextWordsBigram(inputText, uniGramList, biGramList);

        StringBuilder wordDisplay = new StringBuilder();
        for (VocabularyObject vocabObj : mostLikelyFollowingWords) {
            wordDisplay.append(vocabObj.getWords()).append(" ");
        }
        mostLikelyWordsLabel.setText("Likely Following Words(Uni Bi-gram): " + wordDisplay);
    }

    private void biGramTriGram() {
        String inputText = inputTextField.getText();
        List<VocabularyObject> mostLikelyFollowingWords = getMostLikelyNextWordsTriGram(inputText, uniGramList, biGramList, triGramList);

        StringBuilder wordDisplay = new StringBuilder();
        for (VocabularyObject vocabObj : mostLikelyFollowingWords) {
            wordDisplay.append(vocabObj.getWords()).append(" ");
        }
        mostLikelyWordsLabel.setText("Likely Following Words(Tri-gram): " + wordDisplay);
    }

    private VocabularyListResultWrapper readUniGram() {

        try (BufferedReader reader = new BufferedReader(new FileReader("news.txt"))) {

            String uniline;
            MyHashTable uniGramHead = new MyHashTable(257);
            while ((uniline = reader.readLine()) != null) {
                String[] words = uniline.split("\\s+");
                for (String word : words) {
                    uniGramHead.setWord(word);
                }
            }
            uniGramList = uniGramHead.fetchVocabularyList();
            System.out.println(uniGramList);


        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage());
        }
        return uniGramList;
    }

    private VocabularyListResultWrapper readBiGram() {

        try (BufferedReader reader = new BufferedReader(new FileReader("news.txt"))) {
            String line;
            String previous = null;
            MyHashTable biGramHead = new MyHashTable(257);
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");

                //first line bi gram - null case
                if (previous == null && words.length > 1) {
                    biGramHead.setBigramWord(words[0], words[1]);
                    previous = words[0];

                }
                for (int i = 1; i < words.length; i++) {
                    biGramHead.setBigramWord(previous, words[i]);
                    previous = words[i];
                }
                //handling last word of each line
                previous = words[words.length - 1];

            }
            biGramList = biGramHead.fetchVocabularyList();


        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage());
        }
        return biGramList;

    }

    private VocabularyListResultWrapper readTriGram() {

        try (BufferedReader reader = new BufferedReader(new FileReader("news.txt"))) {
            String line;
            String previous1 = null;
            String previous2 = null;
            MyHashTable triGramHead = new MyHashTable(257);

            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");

                // Handle first line and subsequent lines with fewer than 3 words
                if (words.length >= 3) {
                    // First line trigram - null cases
                    if (previous2 == null) {
                        triGramHead.setTrigramWord(words[0], words[1], words[2]);
                        previous2 = words[1];
                        previous1 = words[0];
                    } else {
                        // Subsequent trigrams
                        triGramHead.setTrigramWord(previous1, previous2, words[0]);
                    }

                    for (int i = 1; i < words.length - 1; i++) {
                        triGramHead.setTrigramWord(previous2, words[i], words[i + 1]);
                        previous2 = words[i];
                    }
                }

                // Handle last two words only if the line has enough words
                if (words.length >= 2) {
                    previous2 = words[words.length - 2];
                    previous1 = words[words.length - 1];
                }
            }
            triGramList = triGramHead.fetchVocabularyList();


        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage());
        }
        return triGramList;

    }

    private List<VocabularyObject> getMostLikelyNextWordsBigram(String context, VocabularyListResultWrapper uni, VocabularyListResultWrapper bi) {

        List<VocabularyObject> highestProbabilityObjects = new ArrayList<>();

        try {
            String[] words = context.split("\\s+");
            String lastWord = words[words.length - 1];
            List<String> candidateWords = new ArrayList<>();
            Map<String, Double> wordProbabilities = new HashMap<>();
            double totalCountUnigrams = uni.getVocabObjects().stream().mapToInt(VocabularyObject::getCount).sum();
            List<VocabularyObject> uniObjects = uni.getVocabObjects();
            //unigram probabilities for all words
            for (VocabularyObject wordObj : uniObjects) {
                double unigramProbability = (double) wordObj.getCount() / totalCountUnigrams;
                wordProbabilities.put(wordObj.getWords(), unigramProbability);
                candidateWords.add(wordObj.getWords());
            }
            double uniGramCount = uniObjects.stream().filter(obj -> obj.getWords().equals(lastWord)).mapToDouble(VocabularyObject::getCount).findFirst().orElse(0.0);

            //bigram probabilities
            if (lastWord != null) {
                List<VocabularyObject> bigramWords = new ArrayList<>();
                for (VocabularyObject wordObj : bi.getVocabObjects()) {
                    if (wordObj.getWords().split(" ")[0].equals(lastWord)) {
                        bigramWords.add(wordObj);
                    }
                }
                for (VocabularyObject biGramObj : bigramWords) {

                    double bigramProbability = (double) biGramObj.getCount() / uniGramCount;
                    wordProbabilities.put(biGramObj.getWords(), bigramProbability);

                }
                List<String> biWords = new ArrayList<>();
                for (VocabularyObject word : bigramWords) {
                    biWords.add(word.getWords().split(" ")[1]);
                }
                candidateWords.retainAll(biWords); // Restrict candidates to bigrams only
            }

            double highestProbability = 0.0;

            for (String word : candidateWords) {
                double probability = wordProbabilities.get(lastWord + " " + word);
                if (probability > highestProbability) {
                    VocabularyObject vocabObj = new VocabularyObject(word, 0, probability);
                    highestProbabilityObjects.add(vocabObj);
                }
            }
        } catch (NullPointerException e) {
            System.err.println("Error: NullPointerException occurred.");

        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error: ArrayIndexOutOfBoundsException occurred.");

        }
        return highestProbabilityObjects.stream().sorted(Comparator.comparingDouble(VocabularyObject::getProbability).reversed()).limit(20).toList();
    }


    private List<VocabularyObject> getMostLikelyNextWordsTriGram(String context, VocabularyListResultWrapper uni, VocabularyListResultWrapper bi, VocabularyListResultWrapper tri) {
        List<VocabularyObject> highestProbabilityObjects = new ArrayList<>();

        try {

            String[] words = context.split("\\s+");
            String lastWord = words[words.length - 2] + " " + words[words.length - 1];
            List<String> candidateWords = new ArrayList<>();
            Map<String, Double> wordProbabilities = new HashMap<>();
            double totalCountUniGrams = uni.getVocabObjects().stream().mapToInt(VocabularyObject::getCount).sum();
            List<VocabularyObject> uniObjects = uni.getVocabObjects();
            for (VocabularyObject wordObj : uniObjects) {
                double uniGramProbability = (double) wordObj.getCount() / totalCountUniGrams;
                wordProbabilities.put(wordObj.getWords(), uniGramProbability);
                candidateWords.add(wordObj.getWords());
            }
            double biGramCountLastWord = bi.getVocabObjects().stream().filter(obj -> obj.getWords().equals(lastWord)).mapToDouble(VocabularyObject::getCount).findFirst().orElse(0.0);

            //trigram probabilities
            List<VocabularyObject> triGramWords = new ArrayList<>();
            for (VocabularyObject wordObj : tri.getVocabObjects()) {
                if ((wordObj.getWords().split(" ")[0] + " " + wordObj.getWords().split(" ")[1]).equals(lastWord)) {
                    triGramWords.add(wordObj);
                }
            }
            for (VocabularyObject triGramObj : triGramWords) {
                double triGramProbability = (double) triGramObj.getCount() / biGramCountLastWord;
                wordProbabilities.put(triGramObj.getWords(), triGramProbability);
            }
            List<String> triWords = new ArrayList<>();
            for (VocabularyObject word : triGramWords) {
                triWords.add(word.getWords().split(" ")[2]);
            }
            candidateWords.retainAll(triWords); // Restrict candidates to trigrams only

            double highestProbability = 0.0;

            for (String word : candidateWords) {
                double probability = wordProbabilities.get(lastWord + " " + word);
                if (probability > highestProbability) {
                    VocabularyObject vocabObj = new VocabularyObject(word, 0, probability);
                    highestProbabilityObjects.add(vocabObj);
                }
            }
        } catch (NullPointerException e) {
            System.err.println("Error: NullPointerException occurred.");

        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error: ArrayIndexOutOfBoundsException occurred.");

        }
        return highestProbabilityObjects.stream().sorted(Comparator.comparingDouble(VocabularyObject::getProbability).reversed()).limit(20).toList();
    }

}

