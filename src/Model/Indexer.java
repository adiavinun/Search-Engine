package Model;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Class in charge of creating the inverted index and dictionary
 */
public class Indexer {
    //readFile class
    private ReadFile readFile;
    //parse Class;
    private Parse parse;
    //dictionary contain all the terms from the docs + pointer to line number
    private HashMap<String, Long> mainDictionaryInRam;
    //a temp treeMap used to write to the temp posting files. is cleared every 3000 docs
    private TreeMap<String, String> tempPosting;
    // used when reading the finalPosting.txt for the Load Dictionary in the GUI
    private HashMap<String, Integer[]> postingDictionary;
    // saves the paths of the the temp posting files
    private Queue<String> pathsOfTempPostingFiles;
    // list of all the documents in the corpus
    private HashMap<String, Document> listOfDocs;
    // counter for the docs that have been parsed so far
    private int postedDocsCounter;
    // counter for the current number of temp posting files
    private int numberOfPostingTexts;
    // path the user wants to save the files
    private String pathToSave;
    // the length of all the docs in the corpus
    private int allDocLengths;
    // the average length of all the docs
    private double averageDocLength;
    // path to corpus and stop words
    private String corpusAndStopWordsPath;
    // if with or without stem
    private boolean toStem;

    /**
     * constructor
     * @param corpusAndStopWordsPath - path of corpus and stop words
     * @param pathToSave - path the user wants to save the files
     * @param stem - if to stem or not
     */
    Indexer (String corpusAndStopWordsPath, String pathToSave, boolean stem){
        this.corpusAndStopWordsPath = corpusAndStopWordsPath;
        this.toStem = stem;
        readFile = new ReadFile(corpusAndStopWordsPath);
        parse = new Parse(corpusAndStopWordsPath, stem);
        //add toStem to parse
        Comparator<String> compareLexicographically = new Comparator<String>() {
            //letters first (ignoring differences in case), digits are second and marks (like &%^*%) are the last in priority.
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        };
        mainDictionaryInRam = new HashMap<>();
        tempPosting = new TreeMap<>(compareLexicographically);
        postingDictionary = new HashMap<>();
        pathsOfTempPostingFiles = new LinkedList<>();
        listOfDocs = new HashMap<>();
        postedDocsCounter = 0;
        this.pathToSave = pathToSave;
        numberOfPostingTexts = 1;
    }
    /**
     * the main function in this class. Sends each file to the read file and then to the parse and in the end merges all the temp posting files
     * @param toStem
     * @return true if posting succeeded, else false
     */
    public boolean posting (boolean toStem){
        String pathToSavedPostings = "";
        if(toStem){
            pathToSavedPostings = pathToSave + "\\WithStemming";
        }
        else{
            pathToSavedPostings = pathToSave + "\\WithoutStemming";
        }
        File theDir = new File(pathToSavedPostings);
        if (!theDir.exists()) {
            try{
                theDir.mkdir();
                ArrayList<String> corpusFiles = readFile.readCorpus();
                for (String file: corpusFiles){
                    ArrayList<String[]> docstoParse = readFile.castFileToArrayOfDocs(file);
                    parseFile(docstoParse, pathToSavedPostings);
                }
                if (!tempPosting.isEmpty()){
                    writeTempPostToFile(pathToSavedPostings);
                    tempPosting.clear();
                }
                HashSet<String> suspiciousEntityDic = parse.getSuspiciousEntityDic();
                eraseSuspiciousEntitiesFromMainDic(suspiciousEntityDic);
                mergeAllTempPostingFilesToOnePostingFile(pathToSavedPostings);
                saveAverageDocLength(toStem);
                //write stopwords file to savedFiles
                File source = new File(corpusAndStopWordsPath + "\\stop_words.txt");
                File dest = new File(pathToSave + "\\stop_words.txt");
                try {
                    Files.copy(source.toPath(), dest.toPath());
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }catch(Exception e){
                //e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * removes all the entities that were suspicious from the main dictionary
     * @param suspiciousEntityDic
     */
    private void eraseSuspiciousEntitiesFromMainDic (HashSet<String> suspiciousEntityDic){
        Iterator it = suspiciousEntityDic.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            mainDictionaryInRam.remove(key);
        }
    }

    /**
     * merges all the temp posting files to one final posting file
     * @param pathToSavedPostings
     */
    private void mergeAllTempPostingFilesToOnePostingFile (String pathToSavedPostings){
        while (pathsOfTempPostingFiles.size() > 1) {
            boolean lastTwo = false;
            if (pathsOfTempPostingFiles.size() == 2){
                lastTwo = true;
            }
            String path1 = pathsOfTempPostingFiles.remove();
            String path2 = pathsOfTempPostingFiles.remove();
            try {
                String pathName;
                if (lastTwo) {
                    File dirOfFinalPostingFile = new File(pathToSavedPostings + "\\FinalPosting");
                    if (!dirOfFinalPostingFile.exists())
                        dirOfFinalPostingFile.mkdir();
                    pathName = pathToSavedPostings + "\\FinalPosting\\finalPosting.txt";
                }
                else{
                    pathName = pathToSavedPostings + "\\Postings\\posting" + numberOfPostingTexts + ".txt";
                }
                File f = new File(pathName);
                if (!f.exists()) {
                    f.createNewFile();
                    numberOfPostingTexts++;
                }
                FileWriter fw = new FileWriter(f);
                BufferedWriter writer = new BufferedWriter(fw);
                BufferedReader br1 = new BufferedReader(new FileReader(path1));
                BufferedReader br2 = new BufferedReader(new FileReader(path2));
                String line1 = br1.readLine();
                String line2 = br2.readLine();
                String newLine = "";
                long backSlashNBytes = 2;
                long locationIndex = 0;
                while (line1 != null && line2 != null)
                {
                    String term1 = line1.substring(0, line1.indexOf(":"));
                    String term2 = line2.substring(0, line2.indexOf(":"));

                    while (line1 != null && !mainDictionaryInRam.containsKey(term1.toLowerCase()) && !mainDictionaryInRam.containsKey(term1.toUpperCase())){
                        line1 = br1.readLine();
                        if (line1 != null){
                            term1 = line1.substring(0, line1.indexOf(":"));
                        }
                    }
                    while (line2 != null && !mainDictionaryInRam.containsKey(term2.toLowerCase()) && !mainDictionaryInRam.containsKey(term2.toUpperCase())){
                        line2 = br2.readLine();
                        if (line2 != null){
                            term2 = line2.substring(0, line2.indexOf(":"));
                        }
                    }
                    if (line1 != null && line2 != null) {
                        if (term1.equals(term2)) {
                            newLine = line1 + line2.substring(line2.indexOf(":") + 1);
                            writer.write(newLine);
                            writer.newLine();
                            /*if (lastTwo){
                                mainDictionaryInRam.put(checkHowAppearsInMainDic(term1), locationIndex);
                                locationIndex += newLine.getBytes().length + backSlashNBytes;
                            }*/
                            line1 = br1.readLine();
                            line2 = br2.readLine();

                        } else if ((term1.toLowerCase()).equals(term2.toLowerCase())) {
                            newLine = term1.toLowerCase() + ":" + line1.substring(line1.indexOf(":") + 1) + line2.substring(line2.indexOf(":") + 1);
                            writer.write(newLine);
                            writer.newLine();
                            /*if (lastTwo){
                                mainDictionaryInRam.put(checkHowAppearsInMainDic(term1), locationIndex);
                                locationIndex += newLine.getBytes().length + backSlashNBytes;
                            }*/
                            line1 = br1.readLine();
                            line2 = br2.readLine();
                        } else {
                            if ((term1.toLowerCase()).compareTo(term2.toLowerCase()) > 0) {
                                writer.write(line2);
                                writer.newLine();
                                /*if (lastTwo){
                                    mainDictionaryInRam.put(checkHowAppearsInMainDic(term2), locationIndex);
                                    locationIndex += line2.getBytes().length + backSlashNBytes;
                                }*/
                                line2 = br2.readLine();
                            } else {
                                writer.write(line1);
                                /*if (lastTwo){
                                    mainDictionaryInRam.put(checkHowAppearsInMainDic(term1), locationIndex);
                                    locationIndex += line1.getBytes().length + backSlashNBytes;
                                }*/
                                writer.newLine();
                                line1 = br1.readLine();

                            }
                        }
                    }
                }
                while (line1 != null && line2 == null){
                    String term1 = line1.substring(0, line1.indexOf(":"));
                    while (line1 != null && !mainDictionaryInRam.containsKey(term1)){
                        line1 = br1.readLine();
                        if (line1 != null){
                            term1 = line1.substring(0, line1.indexOf(":"));
                        }
                    }
                    if (line1 != null){
                        writer.write(line1);
                        writer.newLine();
                        /*if (lastTwo){
                            mainDictionaryInRam.put(checkHowAppearsInMainDic(term1), locationIndex);
                            locationIndex += line1.getBytes().length + backSlashNBytes;
                        }*/
                        line1 = br1.readLine();
                    }

                }

                while (line2 != null && line1 == null){
                    String term2 = line2.substring(0, line2.indexOf(":"));
                    while (line2 != null && !mainDictionaryInRam.containsKey(term2)){
                        line2 = br2.readLine();
                        if (line2 != null){
                            term2 = line2.substring(0, line2.indexOf(":"));
                        }
                    }
                    if (line2 != null){
                        writer.write(line2);
                        writer.newLine();
                        /*if (lastTwo){
                            mainDictionaryInRam.put(checkHowAppearsInMainDic(term2), locationIndex);
                            locationIndex += line2.getBytes().length + backSlashNBytes;
                        }*/
                        line2 = br2.readLine();
                    }
                }
                br1.close();
                br2.close();
                writer.close();
                fw.close();
                if (!lastTwo){
                    pathsOfTempPostingFiles.add(pathName);
                }
                File f1 = new File(path1);
                File f2 = new File(path2);
                f1.delete();
                f2.delete();
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }

    /**
     * checks how the term appears in the main dictionary
     * @param term
     * @return
     */
    private String checkHowAppearsInMainDic(String term){
        if (mainDictionaryInRam.containsKey(term.toUpperCase())){
            return term.toUpperCase();
        }
        else return term.toLowerCase();
    }

    /**
     * receives an array list of docs and sends each time one doc to the parse class.
     * every 3000 parsed docs, writes to a text file
     * @param docsToParse - list of docs
     * @param pathToSavedPostings - path to where the user wants to save the files
     */
    public void parseFile (ArrayList<String[]> docsToParse, String pathToSavedPostings) throws IOException {
        String docNum = "";
        String date = "";
        String title = "";
        String text = "";
        for (String[] doc : docsToParse) {
            if (doc != null && doc.length > 0){
                docNum = doc[0];
                date = doc[1];
                title = doc[2];
                text = doc[3];
                Document Document = parse.createDocument(text, docNum, date, title);
                combineDicOfDocWithMainDictionaryInRamAndTempPostDic(Document.getDicOfDoc(), docNum);
                Document.countDocLength();
                Document.removeDicOfDoc();
                //listOfDocs.put(Document.getDocNumber(), Document);
                saveImportantDataDoc(Document, toStem);
                allDocLengths += Document.getDocLength();
                saveEntitiesOfDoc(toStem, Document.getEntities(), Document.getDocNumber());
                Document.removeEntities();
                postedDocsCounter++;
            }
            if (postedDocsCounter % 3000 == 0){
                writeTempPostToFile(pathToSavedPostings);
                tempPosting.clear();
            }
        }
    }

    /**
     * writes everything that is in the tempPosting treemap to a text file
     * @param pathToSavedPostings - path to where the user wants to save the files
     */
    private void writeTempPostToFile(String pathToSavedPostings) {
        try {
            File dirOfPostings = new File(pathToSavedPostings + "\\Postings");
            if (!dirOfPostings.exists())
                dirOfPostings.mkdir();
            String pathName = pathToSavedPostings + "\\Postings\\posting" + numberOfPostingTexts + ".txt";
            File f = new File(pathName);
            if (!f.exists()) {
                f.createNewFile();
                numberOfPostingTexts++;
                pathsOfTempPostingFiles.add(pathName);
            }
            FileWriter fw = new FileWriter(f);
            BufferedWriter writer = new BufferedWriter(fw);
            String term;
            String data;
            Set set = tempPosting.entrySet();
            Iterator it = set.iterator();
            StringBuilder postingString = new StringBuilder("");
            while(it.hasNext()) {
                postingString.setLength(0);
                Map.Entry pair = (Map.Entry)it.next();
                term = (String)pair.getKey();
                data = (String)pair.getValue();
                postingString.append(term);
                postingString.append(":");
                postingString.append(data);
                postingString.append("\n");
                writer.write(postingString.toString());

            }
            writer.close();
            fw.flush();
            fw.close();
        } catch (IOException e) {

        }
    }

    /**
     * adds the specific documents dictionary to the main dictionary
     * and the tempPosting treemap
     * @param dicOfDoc - the documents dictionary
     * @param docNum - the documents number
     */
    private void combineDicOfDocWithMainDictionaryInRamAndTempPostDic (HashMap<String, int[]> dicOfDoc, String docNum){
        Iterator it = dicOfDoc.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            String term = (String)pair.getKey();
            int[] termValue = (int[])pair.getValue();
            addToMainDicRamAndTempPostDicWithLowerOrUpperCase(term, docNum, termValue[0]);
        }
    }
    /**
     * checks if the term appears in the main dictionary and tempPosting treemap
     * and adds it by the asked format
     * @param term - the term
     * @param docNum - the docs number
     * @param tf - term frequency
     */
    private void addToMainDicRamAndTempPostDicWithLowerOrUpperCase (String term, String docNum, int tf){
        long pointer = 0;
        String docsString = "";
        if (term != null && term != ""){
            if (term.equals(term.toUpperCase())){
                if (mainDictionaryInRam.containsKey(term.toUpperCase())){
                    if(tempPosting.containsKey(term.toUpperCase())){
                        docsString = tempPosting.get(term.toUpperCase());
                    }
                    docsString = docsString + docNum + "," + tf + ";";
                    tempPosting.put(term.toUpperCase(), docsString);
                    return;
                }
                if (mainDictionaryInRam.containsKey(term.toLowerCase())){
                    if(tempPosting.containsKey(term.toLowerCase())){
                        docsString = tempPosting.get(term.toLowerCase());
                    }
                    docsString = docsString + docNum + "," + tf + ";";
                    tempPosting.put(term.toLowerCase(), docsString);
                    return;
                }
                docsString = docNum + "," + tf + ";";
                tempPosting.put(term.toUpperCase(), docsString);
                mainDictionaryInRam.put(term.toUpperCase(), pointer);
                return;
            }
            else if (term.equals(term.toLowerCase())){
                if (mainDictionaryInRam.containsKey(term.toLowerCase())){
                    if(tempPosting.containsKey(term.toLowerCase())){
                        docsString = tempPosting.get(term.toLowerCase());
                    }
                    docsString = docsString + docNum + "," + tf + ";";
                    tempPosting.put(term.toLowerCase(), docsString);
                    return;
                }
                if (mainDictionaryInRam.containsKey(term.toUpperCase())){
                    mainDictionaryInRam.remove(term.toUpperCase());
                    mainDictionaryInRam.put(term.toLowerCase(),pointer);
                    if(tempPosting.containsKey(term.toUpperCase())){
                        docsString = tempPosting.get(term.toUpperCase());
                    }
                    docsString = docsString + docNum + "," + tf + ";";
                    tempPosting.remove(term.toUpperCase());
                    tempPosting.put(term.toLowerCase(), docsString);
                    return;
                }
                docsString = docNum + "," + tf + ";";
                tempPosting.put(term.toLowerCase(), docsString);
                mainDictionaryInRam.put(term.toLowerCase(),pointer);
                return;
            }
        }
    }
    /**
     * deletes the final posting file and the memory of the program
     * @return true if the reset succeeded else return false
     */
    public boolean reset() {
        File savedFiles = new File(pathToSave);
        File[] filesInSavedFiles = savedFiles.listFiles();
        for (File file : filesInSavedFiles) {
            if (file.isDirectory() && file.listFiles().length > 0)
                deleteFilesRecursive(file.listFiles());
            file.delete();
        }
        mainDictionaryInRam.clear();
        System.gc();
        return true;
    }
    private void deleteFilesRecursive(File[] files) {
        for (File f : files) {
            if (f.isDirectory() && f.listFiles().length > 0)
                deleteFilesRecursive(f.listFiles());
            f.delete();
        }
    }
    /**
     * displays the dictionary of all the terms in the corpus
     * @return a sorted list of all the terms
     */
    public List<String> displayDictionary(boolean toStem) {
        List<String> dictionaryFromDisk = new LinkedList<>();
        String pathToSavedPostings = "";
        if (toStem) {
            pathToSavedPostings = pathToSave + "\\WithStemming";
        } else {
            pathToSavedPostings = pathToSave + "\\WithoutStemming";
        }
        File finalPosting = new File(pathToSavedPostings + "\\FinalPosting\\finalPosting.txt");
        try {
            BufferedReader bf = new BufferedReader(new FileReader(finalPosting));
            String line = bf.readLine();
            while (line != null && line != "") {
                String[] splitByTerm = line.split(":");
                if (mainDictionaryInRam.containsKey(splitByTerm[0])) {
                    String[] splitByDocTF = splitByTerm[1].split(";");
                    int TFCounter = 0;
                    String[] splitByTF;
                    for (int i = 0; i < splitByDocTF.length; i++){
                        splitByTF = splitByDocTF[i].split(",");
                        TFCounter += Integer.parseInt(splitByTF[1]);
                    }
                    dictionaryFromDisk.add(splitByTerm[0] + " : " + TFCounter);
                }
                line = bf.readLine();
            }
            bf.close();
            Collections.sort(dictionaryFromDisk, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            return dictionaryFromDisk;
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return null;
    }
    /**
     * loads the final posting file from the disk to the memory
     * @return true if the loading succeed, else return false
     */
    public boolean loadDictionaryFromDiskToMemory(boolean toStem) {
        mainDictionaryInRam.clear();
        String pathToSavedPostings = "";
        if (toStem) {
            pathToSavedPostings = pathToSave + "\\WithStemming";
        } else {
            pathToSavedPostings = pathToSave + "\\WithoutStemming";
        }
        File finalPosting = new File(pathToSavedPostings + "\\FinalPosting\\finalPosting.txt");
        try {
            BufferedReader bf = new BufferedReader(new FileReader(finalPosting));
            String line = bf.readLine();
            long backSlashNBytes = 2;
            long locationIndex = 0;
            while (line != null && line != "") {
                String[] splitByTerm = line.split(":");
                String[] splitByDocTF = splitByTerm[1].split(";");
                int TFCounter = 0;
                int DFCounter = 0;
                String[] splitByTF;
                for (int i = 0; i < splitByDocTF.length; i++){
                    splitByTF = splitByDocTF[i].split(",");
                    TFCounter += Integer.parseInt(splitByTF[1]);
                    DFCounter++;
                }
                Integer[] tfdf = new Integer[2];
                tfdf[0] = TFCounter;
                tfdf[1] = DFCounter;
                postingDictionary.put(splitByTerm[0], tfdf);
               /* if (mainDictionaryInRam.get(splitByTerm[0]) != locationIndex){
                    System.out.println("problem with:" + splitByTerm[0]);
                }*/
                mainDictionaryInRam.put(splitByTerm[0], locationIndex);
                locationIndex += line.getBytes().length + backSlashNBytes;
                line = bf.readLine();
            }
            bf.close();
            readFromDataDocFile(toStem);
            readFromAvrLengthFile(toStem);
            readFromDataEntitiesOfDoc(toStem);
            return true;
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return false;
    }

    /**
     * reads from the saved file the average doc length of the entire corpus
     * @param toStem - if with or without stem (different paths)
     */
    private void readFromAvrLengthFile (boolean toStem){
        String pathToSaveExtraData = "";
        if (toStem) {
            pathToSaveExtraData = pathToSave + "\\WithStemming\\ExtraData\\averageDocLength.txt";
        } else {
            pathToSaveExtraData = pathToSave + "\\WithoutStemming\\ExtraData\\averageDocLength.txt";
        }
        try {
            File extraData = new File(pathToSaveExtraData);
            BufferedReader bf = new BufferedReader(new FileReader(extraData));
            String line = bf.readLine();
            while (line != null && line != "") {
                String[] split = line.split(";");
                averageDocLength =Double.parseDouble(split[0]);
                line = bf.readLine();
            }
            bf.close();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
    /**
     * reads from the saved dataDoc file all the information we saved that we needed
     * @param toStem - if with or without stem (different paths)
     */
    private void readFromDataDocFile(boolean toStem){
        String pathToSaveExtraData = "";
        if (toStem) {
            pathToSaveExtraData = pathToSave + "\\WithStemming\\ExtraData\\dataDoc.txt";
        } else {
            pathToSaveExtraData = pathToSave + "\\WithoutStemming\\ExtraData\\dataDoc.txt";
        }
        try {
            File extraData = new File(pathToSaveExtraData);
            BufferedReader bf = new BufferedReader(new FileReader(extraData));
            String line = bf.readLine();

            String title = "";
            double docLength = 0;
            while (line != null && !line.equals("") && !line.equals("\n")) {
                String[] splitByDocNum = line.split("@");
                String[] splitByData = splitByDocNum[1].split(";");
                docLength = Double.parseDouble(splitByData[0]);
                title = splitByData[1];
                Document doc = new Document(0, 0, splitByDocNum[0],title,null,null);
                listOfDocs.put(splitByDocNum[0], doc);
                doc.setDocLength(docLength);
                line = bf.readLine();
            }

            bf.close();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
    /**
     * reads from the saved EntitiesOfDoc file all the information we saved that we needed about the entities
     * @param toStem - if with or without stem (different paths)
     */
    private void readFromDataEntitiesOfDoc(boolean toStem){
        String pathToSaveExtraData = "";
        if (toStem) {
            pathToSaveExtraData = pathToSave + "\\WithStemming\\ExtraData\\EntitiesOfDoc.txt";
        } else {
            pathToSaveExtraData = pathToSave + "\\WithoutStemming\\ExtraData\\EntitiesOfDoc.txt";
        }

        try {
            File extraData = new File(pathToSaveExtraData);
            BufferedReader bf = new BufferedReader(new FileReader(extraData));
            String line = bf.readLine();

            String docNumber = "";
            double docLength = 0;
            String[] topFiveEntity;
            int[] topFiveEntityTF;
            while (line != null && !line.equals("") && !line.equals("\n")) {
                int index = line.indexOf(":");
                if (line.length() == index+1){
                    line = bf.readLine();
                    continue;
                }
                topFiveEntity = new String[5];
                int countToFive = 0;
                String[] splitByDocNum = line.split(":");
                String[] splitByEntity = null;
                if (splitByDocNum[1] != null || !splitByDocNum[1].equals("")) {
                    splitByEntity = splitByDocNum[1].split(";");
                }
                docNumber = splitByDocNum[0];
                String name="";
                String tf="";
                for(int i = 0; i < splitByEntity.length && countToFive < 5; i++){
                    String[] splitByTf = splitByEntity[i].split("@");
                    name = splitByTf[0];
                    tf = splitByTf[1];
                    if (mainDictionaryInRam.containsKey(name)){
                        topFiveEntity[countToFive] = name + " ; " + (int)Double.parseDouble(tf);
                        countToFive++;
                    }
                }
                Document doc = listOfDocs.get(docNumber);
                doc.setTopFiveEntities(topFiveEntity);
                line = bf.readLine();
            }

            bf.close();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    /**
     * writes all the important information about the document to the file dataDoc
     * @param doc - the document we want to save its information
     * @param toStem - if with or without stem (different paths)
     * @throws IOException
     */
    private void saveImportantDataDoc (Document doc, boolean toStem) throws IOException {
        String pathName="";
        if (toStem) {
            pathName = pathToSave + "\\WithStemming\\ExtraData";
        } else {
            pathName = pathToSave + "\\WithoutStemming\\ExtraData";
        }
        File dirOfDocData = new File(pathName);
        if (!dirOfDocData.exists())
            dirOfDocData.mkdir();
        File resultsFile = new File(pathName + "\\dataDoc.txt");
        if (!resultsFile.exists()) {
            resultsFile.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(resultsFile, true));
        String toWrite = doc.getDocNumber() + "@" + doc.getDocLength() +";" + doc.getTitle() + ";" + doc.getMax_tf() + ";" + doc.getNumOfUniqueWords() + "\n";
        bw.write(toWrite);
        bw.flush();
        bw.close();
    }

    /**
     * writes the average doc length to the file averageDocLength
     * @param toStem - if with or without stem (different paths)
     * @throws IOException
     */
    private void saveAverageDocLength (boolean toStem) throws IOException {
        String pathName="";
        if (toStem) {
            pathName = pathToSave + "\\WithStemming\\ExtraData";
        } else {
            pathName = pathToSave + "\\WithoutStemming\\ExtraData";
        }
        averageDocLength = allDocLengths / postedDocsCounter;
        File dirOfDocData = new File(pathName);
        if (!dirOfDocData.exists())
            dirOfDocData.mkdir();
        File resultsFile = new File(pathName + "\\averageDocLength.txt");
        if (!resultsFile.exists()) {
            resultsFile.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(resultsFile, true));
        String toWrite = averageDocLength + "\n";
        bw.write(toWrite);
        bw.flush();
        bw.close();
    }

    /**
     * writes all the docs entities to the file EntitiesOfDoc
     * @param toStem - if with or without stem (different paths)
     * @param entities - the docs entities
     * @param docNumber - the doc number
     * @throws IOException
     */
    private void saveEntitiesOfDoc (boolean toStem, LinkedHashMap<String, Integer> entities, String docNumber) throws IOException {
        String pathName="";
        if (toStem) {
            pathName = pathToSave + "\\WithStemming\\ExtraData";
        } else {
            pathName = pathToSave + "\\WithoutStemming\\ExtraData";
        }
        File dirOfDocData = new File(pathName);
        if (!dirOfDocData.exists())
            dirOfDocData.mkdir();
        File resultsFile = new File(pathName + "\\EntitiesOfDoc.txt");
        if (!resultsFile.exists()) {
            resultsFile.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(resultsFile, true));
        Set set = entities.entrySet();
        Iterator it = set.iterator();
        String toWrite = docNumber + ":";
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String entity = (String) pair.getKey();
            int tf = (int) pair.getValue();
            toWrite = toWrite + entity + "@" + tf + ";";
        }
        toWrite += "\n";
        bw.write(toWrite);
        bw.flush();
        bw.close();
    }

    /**
     * getters
     */
    public int getNumberOfUniqueTerms (){
        return mainDictionaryInRam.size();
    }
    public int getNumberOfDocs(){
        return postedDocsCounter;
    }
    public HashMap<String, Long> getMainDictionaryInRam(){
        return mainDictionaryInRam;
    }
    public HashMap<String, Document> getListOfDocs(){
        return listOfDocs;
    }
    public double getAverageDocLength(){
        return averageDocLength;
    }

}
