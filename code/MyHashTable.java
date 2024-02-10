import java.util.*;

public class MyHashTable {


    private final MyLinkedObject linkedObject;
    private static MyHashImplementation hashObject = null;
    private static ArrayList<MyLinkedObject> hashBucketArray;

    // current Hashtable size
    private static int hashTableSize;

    //number of Buckets in Hashtable to handle Collision
    private static int UniqueBucketCount;

    //total buckets for stats calculation
    private static int totalBucketCount;


    // to manage UniqueBucketCount based on new hash index
    private static Set<Integer> usedHashIndices = null;


    public MyHashTable(int m) {
        hashBucketArray = new ArrayList<>();
        usedHashIndices = new HashSet<>();
        //prime number 257 for suitable distribution of data
        hashTableSize = 257;
        UniqueBucketCount = 0;
        totalBucketCount = 0;
        // Create empty buckets
        for (int i = 0; i < hashTableSize; i++) {
            hashBucketArray.add(null);

        }
        linkedObject = new MyLinkedObject();
        hashObject = new MyHashImplementation(m);
    }

    private static class MyLinkedObject implements MyLinkedInterface {

        private final String word;
        private int count;
        private MyLinkedObject next;


        public MyLinkedObject(String w) {
            this.word = w;
            this.count = 1;
            this.next = null;

        }

        public MyLinkedObject() {
            this.word = "";
        }


        public void setWord(String w) {
            int hashTableIndex = hashObject.calcHashDivision(w);
            //Increment bucket count for every new hash index
            if (!usedHashIndices.contains(hashTableIndex)) {
                UniqueBucketCount++;
                totalBucketCount++;
                usedHashIndices.add(hashTableIndex);
            }
            MyLinkedObject head = hashBucketArray.get(hashTableIndex);
            MyLinkedObject newHead = processWord(head, w);
            hashBucketArray.set(hashTableIndex, newHead);
            hashTableReallocate("uni-gram");
        }

        private MyLinkedObject processWord(MyLinkedObject current, String w) {
            try {
                //new node insertion condition : if new head or word is alphabetically smaller than current node insert at the start or in between respectively
                if (current == null || w.compareTo(current.word) < 0) {
                    MyLinkedObject newNode = new MyLinkedObject(w);
                    newNode.next = current;
                    return newNode;
                }
                //increment count if word exists
                else if (w.equals(current.word)) {
                    current.count++;
                    return current;
                }
                //if larger than current and is a new word
                else {
                    current.next = processWord(current.next, w);
                    return current;
                }
            } catch (NullPointerException e) {
                System.err.println("Null value encountered: " + e.getMessage());
                return null;
            } catch (StackOverflowError e) {
                System.err.println("Stack overflow occurred: " + e.getMessage());
                throw new IllegalStateException("vocabulary processing exceeded maximum recursion depth");
            }
        }

        // If bucket occupancy goes beyond 70% increase the hashtable size by 2x
        private void hashTableReallocate(String ngramType) {

            if ((1.0 * UniqueBucketCount) / hashTableSize > 0.7) {
                ArrayList<MyLinkedObject> bucketArray = hashBucketArray;
                hashBucketArray = new ArrayList<>();
                hashTableSize = 2 * hashTableSize;
                UniqueBucketCount = 0;
                for (int i = 0; i < hashTableSize; i++) {
                    hashBucketArray.add(null);

                }

                if (ngramType.equals("uni-gram")) {
                    for (MyLinkedObject headNode : bucketArray) {
                        while (headNode != null) {
                            setWord(headNode.word);
                            headNode = headNode.next;
                        }
                    }
                } else {
                    for (MyLinkedObject headNode : bucketArray) {
                        while (headNode != null) {
                            setNGramWord(headNode.word);
                            headNode = headNode.next;
                        }
                    }
                }
            }
        }

        public VocabularyListResultWrapper fetchVocabularyList() {

            List<VocabularyObject> listWords = new ArrayList<>();
            for (int i = 0; i < hashTableSize; i++) {
                MyLinkedObject head = hashBucketArray.get(i);

                while (head != null) {
                    VocabularyObject listWord = new VocabularyObject(head.word, head.count, 0.0);
                    listWords.add(listWord);
                    head = head.next;

                }
            }
            double standardDeviation = getStandardDeviation(listWords);
            return new VocabularyListResultWrapper(listWords, standardDeviation);
        }


        public void setNGramWord(String w) {
            int hashTableIndex = hashObject.calcHashDivision(w);

            // Increment bucket count for every new hash index
            if (!usedHashIndices.contains(hashTableIndex)) {
                UniqueBucketCount++;
                totalBucketCount++;
                usedHashIndices.add(hashTableIndex);
            }

            MyLinkedObject head = hashBucketArray.get(hashTableIndex);

            if (head == null) {
                MyLinkedObject newNode = new MyLinkedObject(w);
                hashBucketArray.set(hashTableIndex, newNode);
            } else {
                MyLinkedObject current = head;
                while (true) {
                    if (w.equals(current.word)) {
                        current.count++;
                        break;
                    } else if (current.next == null) {
                        current.next = new MyLinkedObject(w);
                        break;
                    } else {
                        current = current.next;
                    }
                }
            }

            hashTableReallocate("n-gram");
        }


    }


    private static class MyHashImplementation extends MyHashFunction {

        private final int hashTableSize;

        public MyHashImplementation(int hashTableSize) {

            this.hashTableSize = hashTableSize;
        }

        public int calcHashDivision(String hashInput) {

            return hashInput.codePointAt(0) % this.hashTableSize;
        }

        public int calcHashFolding(String hashInput) {
            int segment = 2;
            int hash = 0;

            for (int i = 0; i < hashInput.length(); i += segment) {
                String segmentWord = hashInput.substring(i, Math.min(i + segment, hashInput.length()));
                for (char ch : segmentWord.toCharArray()) {
                    hash += ch;
                }
            }

            return hash % this.hashTableSize;
        }
    }


    public int size() {

        return totalBucketCount;
    }

    public boolean isEmpty() {

        return size() == 0;
    }

    public void setWord(String w) {

        linkedObject.setWord(w);

    }

    public void setBigramWord(String w1, String w2) {

        String bigramWord = w1 + " " + w2;
        linkedObject.setNGramWord(bigramWord);

    }

    public void setTrigramWord(String w1, String w2, String w3) {

        String trigramWord = w1 + " " + w2 + " " + w3;
        linkedObject.setNGramWord(trigramWord);

    }

    public VocabularyListResultWrapper fetchVocabularyList() {

        return linkedObject.fetchVocabularyList();

    }

    private static double getStandardDeviation(List<VocabularyObject> listWords) {
        long UniqueObjects = listWords.size();
        double sumSquaredDifferences = getSumSquaredDifferences((double) UniqueObjects);
        double averageSquaredDifferences = sumSquaredDifferences / totalBucketCount;
        return Math.sqrt(averageSquaredDifferences);
    }

    private static double getSumSquaredDifferences(double UniqueObjects) {
        double average = (UniqueObjects / totalBucketCount);
        double sumSquaredDifferences = 0;
        for (int i = 0; i < hashTableSize; i++) {
            MyLinkedObject head = hashBucketArray.get(i);
            int linkedObjCount = 0;
            while (head != null) {
                head = head.next;
                linkedObjCount++;
            }
            if (linkedObjCount > 0) {
                double difference = linkedObjCount - average;
                sumSquaredDifferences += difference * difference;

            }
        }
        return sumSquaredDifferences;
    }

}
