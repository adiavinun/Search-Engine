package Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * This class is in charge of reading the corpus files.
 */
public class ReadFile {
    //path to corpus file
    private String corpusPath;

    /**
     * Constructor
     * @param corpusPath - path of the corpus
     */
    public ReadFile (String corpusPath){
        this.corpusPath = corpusPath;
    }

    /**
     * Reads all files from corpus and returns the paths to all the files in the corpus
     * @return array list of Strings, each String is a path the file texts
     */
    public ArrayList<String> readCorpus(){
        ArrayList<String> filePaths = new ArrayList<>();
        File corpusFolder = new File(corpusPath);
        File[] corpusFiles = corpusFolder.listFiles();
        for (File f: corpusFiles){
            if (f.isDirectory()){
                addFilePathtoList(filePaths, f);
            }
        }
        return filePaths;
    }
    /**
     *Called from function "readCorpus". Inserts file paths into array list from the given file, if the file is a directory, opens it and takes the paths of the files inside it.
     * @param filePaths - current file/directory path
     * @param f - current file
     */
    private void addFilePathtoList (ArrayList<String> filePaths, File f){
        if (filePaths != null && f != null) {
            for (File file : f.listFiles()) {
                if (file.isDirectory()) {
                        addFilePathtoList(filePaths, file);
                    } else {
                        filePaths.add(file.getAbsolutePath());
                    }

            }
        }
    }

    /**
     * Seperates the documents by the Tag <DOC> and saves the relevant data for every document in an ArrayList
     * @param filePath - current file/directory path
     * @return - ArrayList - list of String[] with the relevant data for each doc
     */
    public ArrayList<String[]> castFileToArrayOfDocs (String filePath){
        ArrayList<String[]> stringOfDocs = new ArrayList<>();
        String[] docArray = new String[4];
        File file = new File(filePath);
        String stringOfEntireFile = castFileToString(file);
        String[] splittedDocs = splitStringByTag(stringOfEntireFile, "<DOC>\n");
        String[] splitter = new String[2];
        String[] splitter2 = new String[2];
        for (String doc : splittedDocs){
            if (doc != null && !doc.equals("") && !doc.equals("\n")){
                splitter[0] = "";
                splitter[1] = "";
                splitter2[0] = "";
                splitter2[1] = "";
                splitter = splitStringByTag(doc, "<DOCNO>");
                splitter2 = splitStringByTag(splitter[1], "</DOCNO>");
                docArray[0] = removeSpaces(splitter2[0]); //DocNo
                splitter[0] = "";
                splitter[1] = "";
                if (doc.contains("<DATE1>")){
                    splitter = splitStringByTag(doc, "<DATE1>");
                    splitter2 = splitStringByTag(splitter[1], "</DATE1>");
                    docArray[1] = removeSpaces(splitter2[0]); //Date
                }
                else if (doc.contains("<DATE>")){
                    splitter = splitStringByTag(doc, "<DATE>");
                    splitter2 = splitStringByTag(splitter[1], "</DATE>");
                    docArray[1] = removeSpaces(splitter2[0]); //Date
                }
                else{
                    docArray[1] = ""; //Date
                }
                splitter[0] = "";
                splitter[1] = "";
                if (doc.contains("<TI>")){
                    splitter = splitStringByTag(doc, "<TI>");
                    splitter2 = splitStringByTag(splitter[1], "</TI>");
                    splitter2[0] = splitter2[0].replace("\n", "");
                    docArray[2] = removeSpaces(splitter2[0]); //Title
                    splitter[0] = "";
                    splitter[1] = "";
                }
                else{
                    docArray[2] = "";
                }
                if (doc.contains("<TEXT>")){
                    splitter = splitStringByTag(doc, "<TEXT>");
                    splitter2 = splitStringByTag(splitter[1], "</TEXT>");
                    docArray[3] = removeSpaces(splitter2[0]); //Text
                    String[] docArrayToAdd = new String[4];
                    docArrayToAdd[0] = docArray[0];
                    docArrayToAdd[1] = docArray[1];
                    docArrayToAdd[2] = docArray[2];
                    docArrayToAdd[3] = docArray[3];
                    stringOfDocs.add(docArrayToAdd);

                }
                else{
                    docArray[3] = "";
                }
                docArray[0] = "";
                docArray[1] = "";
                docArray[2] = "";
                docArray[3] = "";
            }
        }
        return stringOfDocs;
    }

    /**
     * called by "castFileToArrayOfDocs". helps remove unwanted spaces in the tags
     * @param str - string
     * @return - string after removed spaces
     */
    private String removeSpaces (String str){
        boolean entered = true;
        while (str.length() > 1 && entered){
            entered = false;
            if ((str.charAt(0)) == ' ' || str.charAt(0) == '\n'){
                str = str.substring(1);
                entered = true;
            }
            if ((str.charAt(str.length()-1)) == ' ' || (str.charAt(str.length()-1)) == '\n'){
                str = str.substring(0, str.length()-1);
                entered = true;
            }
        }
        return str;
    }

    /**
     * called by "castFileToArrayOfDocs". converts file to string
     * @param file - The file we want to convert
     * @return - the string of the file
     */
    private String castFileToString(File file) {
        {
            String stringFile = "";
            try {
                stringFile = new String ( Files.readAllBytes( Paths.get(file.toPath().toString()) ) );
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return stringFile;
        }
    }

    /**
     * called by "castFileToArrayOfDocs". splits a string by the given tag
     * @param str - the string we want to split
     * @param wordSplit - the tag we want to split by
     * @return String[] the splitted string
     */
    private String[] splitStringByTag (String str, String wordSplit){
        String[] splittedString = new String[2];
        splittedString[0] = "";
        splittedString[1] = "";
        if ((str != null || !str.equals("") && str.contains(wordSplit))){
            splittedString = str.split(wordSplit);
            return splittedString;
        }
        return splittedString;
    }

    /**
     * reads the query file and saves the relevant information text in an array
     * @param filePath - the path of query file
     * @return - list of the arrays of all the queries from the file
     */
    public ArrayList<String[]> readQuery (String filePath){
        ArrayList<String[]> listOfQuerys = new ArrayList<>();
        String[] queryArray = new String[4];
        File file = new File(filePath);
        String stringOfEntireFile = castFileToString(file);
        String[] splittedQuerys = splitStringByTag(stringOfEntireFile, "<top>\n");
        String[] splitter = new String[2];
        String[] splitter2 = new String[2];
        for (String query : splittedQuerys){
            if (query != null && !query.equals("") && !query.equals("\n")){
                splitter[0] = "";
                splitter[1] = "";
                splitter2[0] = "";
                splitter2[1] = "";
                splitter = splitStringByTag(query, "<num> Number: ");
                splitter2 = splitStringByTag(splitter[1], "<title> ");
                queryArray[0] = removeSpaces(splitter2[0]); //Number
                splitter[0] = "";
                splitter[1] = "";
                if (query.contains("<desc> Description:")){
                    splitter = splitStringByTag(splitter2[1], "<desc> Description:");
                    queryArray[1] = removeSpaces(splitter[0]); //title
                }
                else{
                    queryArray[1] = ""; //title
                }
                splitter2[0] = "";
                splitter2[1] = "";
                if (query.contains("<narr> Narrative: ")){
                    splitter2 = splitStringByTag(splitter[1], "<narr> Narrative: ");
                    splitter2[0] = removeSpaces(splitter2[0]);
                    queryArray[2] = removeRepeatedWords(splitter2[0]); //desc
                }
                else{
                    queryArray[2] = "";
                }
                splitter[0] = "";
                splitter[1] = "";
                splitter = splitStringByTag(splitter2[1], "</top>");
                splitter[0] = removeSpaces(splitter[0]);
                queryArray[3] = removeRepeatedWords(splitter[0]); //narr
                String[] queryArrayToAdd = new String[4];
                queryArrayToAdd[0] = queryArray[0];
                queryArrayToAdd[1] = queryArray[1];
                queryArrayToAdd[2] = queryArray[2];
                queryArrayToAdd[3] = queryArray[3];
                listOfQuerys.add(queryArrayToAdd);
                queryArray[0] = "";
                queryArray[1] = "";
                queryArray[2] = "";
                queryArray[3] = "";
            }
        }
        return listOfQuerys;
    }

    /**
     * removes words that appear in most queries and are not "relevant" words
     * @param str - the string to remove repeated words
     * @return - the string after the repeated words have been removed
     */
    private String removeRepeatedWords (String str){
        str = str.replace("documents", "");
        str = str.replace("discussing", "");
        str = str.replace("Discussing", "");
        str = str.replace("must", "");
        str = str.replace("Documents", "");
        str = str.replace("Relevant", "");
        str = str.replace("relevant", "");
        str = str.replace("non-relevant", "");
        str = str.replace("Non-Relevant", "");
        str = str.replace("Identify", "");
        str = str.replace("identified", "");
        str = str.replace("identify", "");
        str = str.replace("document", "");
        str = str.replace("Document", "");
        str = str.replace("discuss", "");
        str = str.replace("concerns", "");
        str = str.replace("considered", "");
        str = str.replace("concern", "");
        str = str.replace("contain", "");
        str = str.replace("contains", "");
        str = str.replace("information", "");
        str = str.replace("Information", "");
        str = str.replace("Find", "");
        str = str.replace("issues", "");
        str = str.replace("impact", "");
        str = str.replace("following", "");
        return str;
    }
}
