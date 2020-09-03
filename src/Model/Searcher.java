package Model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class Searcher {
    // parse class
    Parse parse;
    // ranker class
    Ranker ranker;
    // the main dictionary
    private HashMap<String, Long> mainDictionaryInRam;

    public Searcher(Parse parse, Indexer indexer, HashMap<String, Document> docs) {
        this.parse = parse;
        this.mainDictionaryInRam = indexer.getMainDictionaryInRam();
        ranker = new Ranker(docs, indexer.getAverageDocLength());
    }

    /**
     * parses the query, and calls the ranker to rank all of its relevant docs
     * @param Query - the query
     * @param toStem - if with or without stem
     * @param pathToSave - path to the saved files
     * @return the ranked docs
     */
    public HashMap<String, Double> runQuery(String[] Query, boolean toStem, String pathToSave) {
        HashMap<String, int[]> tempWordsInQuery = new HashMap<>();
        //check if the hashmap is updated correctly
        parse.parseQuery(tempWordsInQuery, Query[1] + " " + Query[2]);
        String pathToReadFrom;
        if (toStem) {
            pathToReadFrom = pathToSave + "\\WithStemming";
        } else pathToReadFrom = pathToSave + "\\WithoutStemming";
        Set<String> keys = tempWordsInQuery.keySet();
        String termInDic = "";
        HashSet<String> docsID = new HashSet<>();
        HashMap<String, QueryWord> wordsInQuery = new HashMap<>();
        //foreach word in query
        for (String term : keys) {
            if (mainDictionaryInRam.containsKey(term.toLowerCase())) {
                termInDic = term.toLowerCase();
            } else if (mainDictionaryInRam.containsKey(term.toUpperCase()))
                termInDic = term.toUpperCase();
            else
                continue;
            long pointer = mainDictionaryInRam.get(termInDic);
            HashMap<String, Integer> tfPerDoc = new HashMap<>();
            int DFCounter = 0;
            int tfDoc = 0;
            try {
                RandomAccessFile raf = new RandomAccessFile(pathToReadFrom + "\\FinalPosting\\finalPosting.txt", "rw");
                raf.seek(pointer);
                String lineFromPosting = raf.readLine();
                if (lineFromPosting != null && lineFromPosting != "") {
                    //System.out.println(lineFromPosting);
                    String[] splitByTerm = lineFromPosting.split(":");
                    String[] splitByDocTF = splitByTerm[1].split(";");
                    String[] splitByDF;
                    for (int i = 0; i < splitByDocTF.length; i++) {
                        splitByDF = splitByDocTF[i].split(",");
                        docsID.add(splitByDF[0]);
                        tfDoc = Integer.parseInt(splitByDF[1]);
                        DFCounter++;
                        tfPerDoc.put(splitByDF[0], tfDoc);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            boolean isInTitle = false;
            boolean isInDesc = false;
            boolean isInNarr = false;
            if (Query[1].toLowerCase().contains(term.toLowerCase())) {
                isInTitle = true;
            }
            if (Query[2].toLowerCase().contains(term.toLowerCase())) {
                isInDesc = true;
            }
            if (Query[3].toLowerCase().contains(term.toLowerCase())) {
                isInNarr = true;
            }
            QueryWord queryWord = new QueryWord(term, tfPerDoc, DFCounter, isInTitle, isInDesc, isInNarr);
            wordsInQuery.put(term, queryWord);
        }
        return sortAndReturn50RelevantDocs(ranker.rankAllDocs(wordsInQuery, docsID));
    }
    /**
     * sorts the ranked docs and returns the top 50
     * URL used: https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values
     * @param rankedDocs - the ranked docs
     * @return - the top 50 ranked docs
     */
    private HashMap<String, Double> sortAndReturn50RelevantDocs (HashMap<String, Double> rankedDocs){
        List<Map.Entry<String, Double>> list = new LinkedList<>(rankedDocs.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        HashMap finalRankedDocs = new LinkedHashMap<String, Double>();
        int docLimit = Math.min(50, list.size());
        for (int i = 0; i < docLimit; i++) {
            Map.Entry docPair = list.get(i);
            finalRankedDocs.put(docPair.getKey(), docPair.getValue());
        }
        return finalRankedDocs;
    }

    /**
     * returns the docs entities
     * @param docID - the docs ID
     * @return - list of entities
     */
    public List<String> getEntities(String docID){
        return ranker.getEntities(docID);
    }
}