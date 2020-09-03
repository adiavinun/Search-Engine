package Model;

import java.util.HashMap;
import java.util.HashSet;

public class QueryWord {
    // the word
    private String word;
    // HashMap with DocId and tf
    private HashMap<String, Integer> tfPerDoc;
    // the words document frequeny
    private int df;
    // if the word appears in the title
    private boolean isInTitle;
    // if the word appears in the description
    private boolean isInDesc;
    // if the word appears in the narrative
    private boolean isInNarr;

    QueryWord (String word, HashMap<String, Integer> tfPerDoc, int df, boolean isInTitle, boolean isInDesc, boolean isInNarr){
        this.word = word;
        this.tfPerDoc = tfPerDoc;
        this.df = df;
        this.isInTitle = isInTitle;
        this.isInDesc = isInDesc;
        this.isInNarr = isInNarr;
    }

    /**
     * getters
     */
    public HashMap<String, Integer> getTfPerDoc(){
        return tfPerDoc;
    }
    public int getDf(){
        return df;
    }
    public String getWord(){
        return word;
    }
    public boolean getIsInTitle(){
        return isInTitle;
    }
    public boolean getIsInDesc(){
        return isInDesc;
    }
    public boolean getIsInNarr(){
        return isInNarr;
    }



}
