public class VocabularyObject {

    private final String word;
    private final int count;
    private double probability;

    public VocabularyObject(String word, int count, double probability) {
        this.word = word;
        this.count = count;
        this.probability = probability;
    }

    public String getWords() {
        return word;
    }

    public int getCount() {
        return count;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}