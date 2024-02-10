import java.util.List;

public class VocabularyListResultWrapper {

    private final List<VocabularyObject> vocabObjects;
    private final double standardDeviation;

    public VocabularyListResultWrapper(List<VocabularyObject> vocabObjects, double standardDeviation) {
        this.vocabObjects = vocabObjects;
        this.standardDeviation = standardDeviation;
    }

    public List<VocabularyObject> getVocabObjects() {
        return vocabObjects;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }
}

