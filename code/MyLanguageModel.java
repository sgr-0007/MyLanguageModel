import java.awt.*;
import javax.swing.*;

public class MyLanguageModel extends JFrame {

    public MyLanguageModel() {
        super("My Language Model");
        setLayout(new FlowLayout());

        JButton newsPreview = new JButton("Preview News");
        newsPreview.addActionListener(e -> new NewsFilePreview().setVisible(true));
        add(newsPreview, BorderLayout.NORTH);

        JButton vocabList = new JButton("News Vocabulary");
        vocabList.addActionListener(e -> new VocabularyList().setVisible(true));
        add(vocabList, BorderLayout.NORTH);

        JButton biiGramLM = getJButton("bi-gram");
        add(biiGramLM, BorderLayout.NORTH);

        JButton triGramLM = getJButton("tri-gram");
        add(triGramLM, BorderLayout.NORTH);
        setSize(600, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton nGramLM = getJButton("prediction");
        add(nGramLM, BorderLayout.NORTH);
        setSize(600, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static JButton getJButton(String type) {
        JButton nGramLM = new JButton("Tri-Gram");
        if (type.equals("tri-gram")) {
            nGramLM = new JButton("Tri Gram");
            nGramLM.addActionListener(e -> {
                JFrame loadingFrame = getLoadingFrame();
                // Load TriGramLM in a separate thread
                new Thread(() -> {
                    new TriGramLM().setVisible(true);
                    SwingUtilities.invokeLater(loadingFrame::dispose);
                }).start();
            });
        } else if(type.equals("bi-gram")) {
            nGramLM = new JButton("Bi Gram");
            nGramLM.addActionListener(e -> {
                JFrame loadingFrame = getLoadingFrame();
                // Load BiGramLM in a separate thread
                new Thread(() -> {
                    new BiGramLM().setVisible(true);
                    SwingUtilities.invokeLater(loadingFrame::dispose);
                }).start();
            });
        }
        else{

            nGramLM = new JButton("Prediction LM");
            nGramLM.addActionListener(e -> {
                JFrame loadingFrame = getLoadingFrame();
                // Load PredictionLM in a separate thread
                new Thread(() -> {
                    new NGramPrediction().setVisible(true);
                    SwingUtilities.invokeLater(loadingFrame::dispose);
                }).start();
            });
        }
        return nGramLM;

    }

    private static JFrame getLoadingFrame() {

        JFrame loadingFrame = new JFrame("Loading");
        loadingFrame.setLayout(new BorderLayout());
        JLabel loadingLabel = new JLabel("Loading, please wait...");
        loadingLabel.setHorizontalAlignment(JLabel.CENTER);
        loadingFrame.add(loadingLabel, BorderLayout.CENTER);
        loadingFrame.setSize(300, 100);
        loadingFrame.setLocationRelativeTo(null);
        loadingFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        loadingFrame.setVisible(true);
        return loadingFrame;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MyLanguageModel().setVisible(true);
        });


    }
}
