package Model;
import java.util.*;

public class Document {

    // the most frequent term in the document
    private double max_tf;
    // number of unique words in the document
    private int numOfUniqueWords;
    // the doc number
    private String docNumber;
    // the docs title
    private String title;
    // dictionary that holds all the terms in the document
    private HashMap<String, int[]> dicOfDoc;
    //the length of the doc (number of words including repeats)
    private double docLength;
    // all the entities from the doc (not for sure entity)
    private HashMap<String, Integer> entities;
    // the docs most frequent entities and tf (top 5)
    private String[] topFiveEntities;

    /**
     * constructor
     * @param docNumber
     * @param title
     * @param dicOfDoc
     */
    public Document (double max_tf, int numOfUniqueWords, String docNumber, String title, HashMap<String, int[]> dicOfDoc, HashMap<String, Integer> entities){
        this.max_tf = max_tf;
        this.numOfUniqueWords = numOfUniqueWords;
        this.docNumber = docNumber;
        //this.date = date;
        this.title = title;
        this.dicOfDoc = dicOfDoc;
        docLength = 0;
        this.entities = entities;
        topFiveEntities = new String[5];
    }

    /**
     * getter for dictionary
     * @return the dictionary of the document
     */
    public HashMap<String, int[]> getDicOfDoc (){
        return dicOfDoc;
    }

    public void countDocLength (){
        double length = 0;
        Set set = dicOfDoc.entrySet();
        Iterator it = set.iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            int[] value = (int[]) pair.getValue();
            length += value[0];
        }
        this.docLength = length;
}

    /**
     * removes the dictionary
     */
    public void removeDicOfDoc (){
        dicOfDoc.clear();
    }

    /**
     * sorts the hashmap entities by its value (tf) and returns the sorted map;
     * used an open code: https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values
     * @return sorted hashmap
     */
    public LinkedHashMap<String, Integer> getEntities(){
        List<Map.Entry<String, Integer>> list = new LinkedList<>(entities.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        LinkedHashMap entities = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < list.size(); i++) {
            Map.Entry entityPair = list.get(i);
            entities.put(entityPair.getKey(), entityPair.getValue());
        }
        return entities;

    }

    /**
     * puts top five entities in a list
     * @return list of top five entities
     */
    public  List<String> getHasMapTopFiveEntities(){
        List<String> fiveEntities = new ArrayList<>();
        for (int i = 0; i < topFiveEntities.length; i++){
            if (topFiveEntities[i] != null) {
                fiveEntities.add(topFiveEntities[i]);
            }
        }
        return fiveEntities;
    }

    /**
     * clears the hash map entities
     */
    public void removeEntities(){
        entities.clear();
    }
    /**
     *
     * getters and setters
     */
    public double getDocLength(){
        return docLength;
    }

    public String getTitle(){
        return title;
    }
    public String getDocNumber(){
        return docNumber;
    }
    public void setDocLength(double length){
        docLength = length;
    }
    public double getMax_tf(){
        return max_tf;
    }
    public int getNumOfUniqueWords(){
        return numOfUniqueWords;
    }

    public String[] getTopFiveEntities() {
        return topFiveEntities;
    }

    public void setTopFiveEntities(String[] topFiveEntities) {
        this.topFiveEntities = topFiveEntities;
    }


}
