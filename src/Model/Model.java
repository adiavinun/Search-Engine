package Model;

import com.medallia.word2vec.Word2VecModel;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

public class Model {
    // indexer class
    private Indexer indexer;
    // searcher class
    private Searcher searcher;

    /**
     * creates the dictionary and the final inverted index posting
     * @param corpusPath - path to corpus
     * @param savedFilesPath - path to saved files
     * @param toStem - if with or without stem
     * @return if succeeded
     */
    public boolean generateInvertedIndex(String corpusPath, String savedFilesPath, boolean toStem){
        File stopWordsFile = new File(corpusPath + "//stop_words.txt");
        if(!stopWordsFile.exists()) {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("ERROR");
            chooseFile.setContentText("The folder you selected does not contain a text file named stop_words.");
            chooseFile.show();
            return false;
        }
        File savedFiles = new File(savedFilesPath);
        if(!savedFiles.exists()) {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("ERROR");
            chooseFile.setContentText("The folder you selected does not exist.");
            chooseFile.show();
            return false;
        }
        indexer = new Indexer(corpusPath, savedFilesPath ,toStem);
        long startTime = System.currentTimeMillis();
        boolean postingSucceeded = indexer.posting(toStem);
        long endTime = System.currentTimeMillis();
        Alert alert;
        if(postingSucceeded){
            long runTime = endTime-startTime;
            int numOfIndexedDocs = indexer.getNumberOfDocs();
            int numOfUniqueTerms = indexer.getNumberOfUniqueTerms();
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Finished!");
            alert.setContentText("Runtime (in seconds): " + runTime/1000 + "\n" +
                    "Number of indexed documents: " + numOfIndexedDocs + "\n" +
                    "Number of unique terms: " + numOfUniqueTerms);
            alert.show();
            return true;
        }
        else{
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("COMMIT");
            alert.setContentText("Sorry, the process failed.");
            alert.show();
            return false;
        }
    }
    /**
     * reset the final posting file and the memory of the program
     * @return true if the reset succeeded else return false
     */
    public boolean reset(){
        return indexer.reset();
    }


    /**
     * displays the dictionary of all the terms in the corpus
     * @return a sorted list of all the terms
     */
    public List<String> displayDictionary(boolean toStem, String savedFilesPath){
        File savedFiles = new File(savedFilesPath);
        if(!savedFiles.exists()) {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("ERROR");
            chooseFile.setContentText("The folder you selected does not exist.");
            chooseFile.show();
            return null;
        }
        if(toStem){
            File withStem = new File(savedFilesPath + "\\withStemming");
            if(!withStem.exists()){
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("ERROR");
                chooseFile.setContentText("The folder you selected does not contain the withStemming folder.");
                chooseFile.show();
                return null;
            }
        }
        else{
            File withoutStem = new File(savedFilesPath + "\\withoutStemming");
            if(!withoutStem.exists()){
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("ERROR");
                chooseFile.setContentText("The folder you selected does not contain the withoutStemming folder.");
                chooseFile.show();
                return null;
            }
        }
        List<String> dictionary = indexer.displayDictionary(toStem);
        if(dictionary == null){
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("ERROR");
            chooseFile.setContentText("We weren't able to find the dictionary.");
            chooseFile.show();
            return null;
        }
        return dictionary;
    }

    /**
     * loads the final posting file from the disk to the memory
     * @return true if the loading succeed, else return false
     */
    public boolean loadDictionaryFromDiskToMemory(boolean toStem, String savedFilesPath){
        File savedFiles = new File(savedFilesPath);
        if(!savedFiles.exists()) {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("ERROR");
            chooseFile.setContentText("The folder you selected does not exist.");
            chooseFile.show();
            return false;
        }
        if(toStem){
            File withStem = new File(savedFilesPath + "\\withStemming");
            if(!withStem.exists()){
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("ERROR");
                chooseFile.setContentText("The folder you selected does not contain the withStemming folder.");
                chooseFile.show();
                return false;
            }
        }
        else{
            File withoutStem = new File(savedFilesPath + "\\withoutStemming");
            if(!withoutStem.exists()){
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("ERROR");
                chooseFile.setContentText("The folder you selected does not contain the withoutStemming folder.");
                chooseFile.show();
                return false;
            }
        }
        if (indexer == null){
            indexer = new Indexer(savedFilesPath, savedFilesPath, toStem);
        }
        boolean bl = indexer.loadDictionaryFromDiskToMemory(toStem);
        if(bl) return true;
        else{
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setContentText("Loading failed!");
            chooseFile.show();
            return false;
        }
    }
    /**
     * finds words that have the same meaning as the given word using the website: https://www.datamuse.com/api/
     * also used open code: https://www.codota.com/code/java/methods/java.net.URLConnection/setAllowUserInteraction
     * @param query - the words in the query
     * @return - the words in the query + the added semantic words
     */
    public String onlineSemanticAlgorithm(String query) {
        //call to api
        String[] queryWords = query.split(" ");
        LinkedList<String> semanticWords = new LinkedList<>();
        try{
            if (queryWords.length > 0) {
                for (String word : queryWords) {
                    URL url = null;
                    url = new URL("https://api.datamuse.com/words?ml=" + word);
                    //make connection
                    URLConnection urlConnection = url.openConnection();
                    //use post mode
                    urlConnection.setDoOutput(true);
                    urlConnection.setAllowUserInteraction(false);
                    //get result
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String currLine = br.readLine();
                    if (currLine != null && !currLine.equals("")) {
                        String[] firstLineArr = currLine.split("\"word\":\"");
                        for (String s : firstLineArr) {
                            int i = 0;
                            String currWord = "";
                            while (s != null && i < s.length() && s.charAt(i) != '\"') {
                                if (Character.isLetter(s.charAt(i)))
                                    currWord += s.charAt(i);
                                i++;
                            }
                            if (currWord != null && currWord.length() > 0) {
                                semanticWords.add(currWord);
                                if (semanticWords.size() % 3 == 0)
                                    break;
                            }
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String word : semanticWords) {
            query += " " + word;
        }
        return query;
    }

    /**
     * finds words that have the same meaning as the given word using the attached jar
     * @param query - the words in the query
     * @return - the words in the query + the added semantic words
     */
    public String offlineSemanticAlgorithm(String query){
        String[] queryWords = query.split(" ");
        if (queryWords.length > 0) {
            for (String word : queryWords) {
                try {
                    Word2VecModel model = Word2VecModel.fromTextFile(new File(".\\resources\\word2vec.c.output.model.txt"));
                    com.medallia.word2vec.Searcher semanticSearcher = model.forSearch();
                    int numOfResultInList = 2;
                    List<com.medallia.word2vec.Searcher.Match> matches = semanticSearcher.getMatches(word, numOfResultInList);
                    boolean first = true;
                    for (com.medallia.word2vec.Searcher.Match match: matches) {
                        if (first){
                            match.match();
                            first = false;
                        }
                        else{
                            String res = match.match();
                            query += " " + res;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (com.medallia.word2vec.Searcher.UnknownWordException e) {
                    //e.printStackTrace();
                }
            }
        }
        return query;
    }

    /**
     * the main function that runs the query that is given from a text file
     * @param pathQueryFile - path to the query file
     * @param toStem - if with or without stem
     * @param corpusPath - path to corpus
     * @param savedFilesPath - path to saved files
     * @param onlineSemantic - if with online semantic
     * @param offlineSemantic - if with offline semantic
     * @param toSaveResults - if user wants to save results
     * @param pathForResults - path to save the query results
     * @return - hash map of all the top 50 ranked docs of every query
     * @throws IOException
     * @throws InterruptedException
     */
    public HashMap<String, HashMap<String, Double>> runQueryFromFile(String pathQueryFile, boolean toStem, String corpusPath, String savedFilesPath, boolean onlineSemantic, boolean offlineSemantic, boolean toSaveResults, String pathForResults) throws IOException, InterruptedException {
        if (toStem) {
            File withStem = new File(savedFilesPath + "\\WithStemming");
            if (!withStem.exists()) {
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("Dictionary doesn't exist!");
                chooseFile.setContentText("Please first select commit with the stemming option and then try again.");
                chooseFile.show();
                return null;
            }
        } else {
            File withoutStem = new File(savedFilesPath + "\\WithoutStemming");
            if (!withoutStem.exists()) {
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("Dictionary doesn't exist!");
                chooseFile.setContentText("Please first select commit with out the stemming option and then try again.");
                chooseFile.show();
                return null;
            }
        }
        ReadFile rf = new ReadFile(savedFilesPath);
        Parse parse = new Parse(savedFilesPath, toStem);
        HashMap<String, Document> docs = indexer.getListOfDocs();
        searcher = new Searcher(parse, indexer, docs);
        ArrayList<String[]> queries = rf.readQuery(pathQueryFile);
        HashMap<String, HashMap<String, Double>> allTheRankedDocs = new HashMap<String, HashMap<String, Double>>();
        for(String[] query : queries){
            if (onlineSemantic) {
                query[1] += onlineSemanticAlgorithm(query[1]);
                //query[2] += semanticAlgorithm(query[2]);
            }else if (offlineSemantic){
                query[1] += offlineSemanticAlgorithm(query[1]);
            }
            HashMap<String, Double> rankedDoc = searcher.runQuery(query, toStem, savedFilesPath);
            allTheRankedDocs.put(query[0], rankedDoc);
        }
        if (toSaveResults) {
            File file = new File(pathForResults);
            if (!file.exists()) {
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("ERROR");
                chooseFile.setContentText("The folder you selected does not exist.");
                chooseFile.show();
            } else {
                File resultsFile = new File(pathForResults + "\\QueryResults.txt");
                if (resultsFile.exists())
                    resultsFile.delete();
                int size = queries.size();
                for (int i = 0; i < size; i++) {
                    String[] tempQ = queries.get(i);
                    writeToQueryTextFile(pathForResults, allTheRankedDocs.get(tempQ[0]), tempQ);
                }
            }
        }
        return allTheRankedDocs;
    }

    /**
     * the main function that runs the query that the user types in
     * @param query - the query the user entered
     * @param toStem - if with or without stem
     * @param savedFilesPath - path to saved files
     * @param corpusPath - path to corpus
     * @param onlineSemantic - if with online semantic
     * @param offlineSemantic - if with offline semantic
     * @param toSaveResults - if user wants to save results
     * @param pathForResults - path to save the query results
     * @return hash map of all the top 50 ranked docs of the query
     */
    public HashMap<String, Double> runQueryFromUser(String query, boolean toStem, String savedFilesPath, String corpusPath, boolean onlineSemantic, boolean offlineSemantic, boolean toSaveResults, String pathForResults) {
        if (toStem) {
            File withStem = new File(savedFilesPath + "\\WithStemming");
            if (!withStem.exists()) {
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("Dictionary doesn't exist!");
                chooseFile.setContentText("Please first select commit with the stemming option and then try again.");
                chooseFile.show();
                return null;
            }
        } else {
            File withoutStem = new File(savedFilesPath + "\\WithoutStemming");
            if (!withoutStem.exists()) {
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("Dictionary doesn't exist!");
                chooseFile.setContentText("Please first select commit with out the stemming option and then try again.");
                chooseFile.show();
                return null;
            }
        }
        //if semantic checkBox selected.
        if (onlineSemantic) {
            query += onlineSemanticAlgorithm(query);
        }
        else if(offlineSemantic){
            query += offlineSemanticAlgorithm(query);
        }
        ReadFile rf = new ReadFile(savedFilesPath);
        Parse parse = new Parse(savedFilesPath, toStem);
        HashMap<String, Document> docs = indexer.getListOfDocs();
        searcher = new Searcher(parse, indexer, docs);
        String[] queryStr = new String[4];
        queryStr[0] = "100";
        queryStr[1] = query;
        queryStr[2] = "";
        queryStr[3] = "";
        HashMap<String, Double> queryResults = searcher.runQuery(queryStr, toStem, savedFilesPath);
        if (toSaveResults) {
            try {
                File file = new File(pathForResults);
                if (!file.exists()) {
                    Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                    chooseFile.setHeaderText("ERROR");
                    chooseFile.setContentText("The folder you selected does not exist.");
                    chooseFile.show();
                } else {
                    File resultsFile = new File(pathForResults + "\\QueryResults.txt");
                    if (resultsFile.exists())
                        resultsFile.delete();
                    writeToQueryTextFile(pathForResults, queryResults, queryStr);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return queryResults;
    }

    /**
     * getter for the docs entities
     * @param docID - the docs id number
     * @return the list of the docs entities
     */
    public List<String> getEntities(String docID){
        return searcher.getEntities(docID);
    }

    /**
     * writes the results of the query to a text file in the treceval format
     * @param pathForResults - path to write the results
     * @param queryResults - the ranked docs
     * @param query - the query
     * @throws IOException
     */
    public void writeToQueryTextFile(String pathForResults, HashMap<String, Double> queryResults, String[] query) throws IOException {
        File resultsFile = new File(pathForResults + "\\QueryResults.txt");
        if (!resultsFile.exists()) {
            resultsFile.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(resultsFile, true));
        for (Map.Entry<String, Double> res : queryResults.entrySet()) {
            String toWrite = query[0] + " 0 " + res.getKey() + " 1 42.38 mt\n";
            bw.write(toWrite);

        }
        bw.flush();
        bw.close();
    }
}
