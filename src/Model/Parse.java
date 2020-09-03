package Model;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Parses all the terms per doc by the given laws
 */
public class Parse {
    // the path of the stop words file
    private String stopWordsPath;
    // hashset of all the stop words
    private HashSet<String> stopWords;
    //hashset of all entities that appear in 2 or more docs
    private HashSet<String> entityDic;
    //hashset of all the entities that appear in only one doc
    private HashSet<String> suspiciousEntityDic;
    //hashmap of all the months
    private HashMap<String, String> months;
    //boolean to know if with or without stem
    boolean stem;
    //Stemmer class
    private Stemmer stemmer;

    /**
     * constructor
     * @param stopWordsPath - path to stopWords txt
     * @param stem - boolean if to stem
     */
    public Parse (String stopWordsPath, boolean stem){
        this.stopWordsPath = stopWordsPath;
        this.stopWords = new HashSet<>();
        this.entityDic =  new HashSet<>();
        this.suspiciousEntityDic =  new HashSet<>();
        insertStopWordsIntoHashSet();
        stemmer = new Stemmer();
        this.stem = stem;
        this.months = new HashMap<>();
        createMonthHS();
    }
    /**
     * adds all the months to the month hashMap
     */
    private void createMonthHS() {
        months.put("January", "01");
        months.put("Jan", "01");
        months.put("JANUARY", "01");
        months.put("JAN", "01");
        months.put("February", "02");
        months.put("Feb", "02");
        months.put("February", "02");
        months.put("FEB", "02");
        months.put("March", "03");
        months.put("Mar", "03");
        months.put("MARCH", "03");
        months.put("MAR", "03");
        months.put("April", "04");
        months.put("Apr", "04");
        months.put("APRIL", "04");
        months.put("APR", "04");
        months.put("MAY", "05");
        months.put("May", "05");
        months.put("June", "06");
        months.put("Jun", "06");
        months.put("JUNE", "06");
        months.put("JUN", "06");
        months.put("July", "07");
        months.put("Jul", "07");
        months.put("JULY", "07");
        months.put("JUL", "07");
        months.put("August", "08");
        months.put("Aug", "08");
        months.put("AUGUST", "08");
        months.put("AUG", "08");
        months.put("September", "09");
        months.put("Sep", "09");
        months.put("SEPTEMBER", "09");
        months.put("SEP", "09");
        months.put("October", "10");
        months.put("Oct", "10");
        months.put("OCTOBER", "10");
        months.put("OCT", "10");
        months.put("November", "11");
        months.put("Nov", "11");
        months.put("NOVEMBER", "11");
        months.put("NOV", "11");
        months.put("December", "12");
        months.put("Dec", "12");
        months.put("DECEMBER", "12");
        months.put("DEC", "12");
    }
    /**
     * splits the texts into terms and checks every term if falls under one of the laws. If it does, it changes the term (depending on the law), else puts the term in the dic the way it is.
     * @param text - the text of the doc
     * @param docNum - the doc number of the doc
     * @param date - the docs date
     * @param title - the docs title
     * @param dicOfText - the dictionary of the text (empty)
     * @return - the dictionary of the text with all the terms
     */
    private HashMap<String, Integer> parseDoc(String text, String docNum, String date, String title, HashMap<String, int[]> dicOfText){
        if(text == null)
            return null;
        String[] terms = text.split(" |\\\n|\\--|\\(|\\)|\\[|\\]|\\)|\\(|\\}|\\{|\\&|\\}|\\:|\\||\\<|\\>|\\?|\\!|\\}|\\_|\\@|\\'\'|\\;|\\\"");
        HashMap<String, Integer> tempEntityDic = new HashMap<>();
        for(int i = 0; i < terms.length; i++){
            String currTerm = terms[i];
            if (currTerm == null || currTerm.equals("") || currTerm.contains("P=") || currTerm.equals("\n")){
                continue;
            }
            if (i < terms.length-1 && !terms[i+1].equals("")){
                if (currTerm.equals("F") && terms[i+1].contains("P=")){
                    continue;
                }
            }

            boolean lastWordInSentence1 = false;
            boolean lastWordInSentence2 = false;
            if (!currTerm.equals("") && currTerm.charAt(currTerm.length()-1) == '.'){
                lastWordInSentence1 = true;
            }
            currTerm = deletePunctuations(currTerm);
            terms[i] = currTerm;
            if (currTerm.equals("")){
                continue;
            }
            if (checkIfStopWord(currTerm.toLowerCase())){
                if (i < terms.length-1 && !terms[i+1].equals("")){
                    if (terms[i+1].charAt(0) <= 'A' && terms[i+1].charAt(0) >= 'Z'){
                        continue;
                    }
                }
            }
            String nextTerm = getNextTerm(terms, i);
            String nextNextTerm = getNextTerm(terms, i+1);
            String nextNextNextTerm = getNextTerm(terms, i+2);
            int used = 0;
            if (checkIfNotNumbers(currTerm)){
                used = removeSlash(currTerm, dicOfText);
                if (used != -1) {
                    i += used;
                    continue;
                }
                int j = i;
                String entity = currTerm;
                while ((j+1 <= terms.length-1 && !terms[j].equals("") && !terms[j+1].equals("") && !lastWordInSentence1 && !lastWordInSentence2 && terms[j].charAt(0) >= 'A' && terms[j].charAt(0) <= 'Z') && (terms[j+1].charAt(0) >= 'A' && terms[j+1].charAt(0) <= 'Z')){
                    if(terms[j+1].charAt(terms[j+1].length()-1) == '.' || terms[j+1].charAt(terms[j+1].length()-1) == ',' || terms[j+1].charAt(terms[j+1].length()-1) == '/'){
                        lastWordInSentence2 = true;
                    }
                    terms[j+1] = deletePunctuations(terms[j+1]);
                    entity = entity + " " + terms[j+1];
                    addToDic(terms[j+1], dicOfText, true);
                    j++;
                }
                if (j > i || lastWordInSentence2){
                    i = j;
                    if (!tempEntityDic.containsKey(entity.toUpperCase())){
                        tempEntityDic.put(entity.toUpperCase(), 1);
                    }
                    else{
                        int value = tempEntityDic.get(entity.toUpperCase());
                        value++;
                        tempEntityDic.put(entity.toUpperCase(), value);
                    }
                    addToDic(entity, dicOfText, false);
                    addToDic(currTerm, dicOfText, true);
                    continue;
                }
                used = castExpressionAndRange(currTerm, nextTerm, nextNextTerm, nextNextNextTerm, dicOfText);
                if (used != -1) {
                    i += used;
                    continue;
                }
                used = removeApostrophe(currTerm, dicOfText);
                if (used != -1) {
                    i += used;
                    continue;
                }
                used = castDate(currTerm, nextTerm, dicOfText);
                if (used != -1) {
                    i += used;
                    continue;
                }
            }
            else{
                used = castExpressionAndRange(currTerm, nextTerm, nextNextTerm, nextNextNextTerm, dicOfText);
                if (used != -1) {
                    i += used;
                    continue;
                }
                used = castDate(currTerm, nextTerm, dicOfText);
                if (used != -1) {
                    i += used;
                    continue;
                }
                used = castDollar(currTerm, nextTerm, nextNextTerm, nextNextNextTerm, dicOfText);
                if (used != -1) {
                    i += used;
                    continue;
                }
                used = castPercent(currTerm, nextTerm, dicOfText);
                if (used != -1) {
                    i += used;
                    continue;
                }
                used = castToKMB(currTerm, nextTerm, dicOfText);
                if (used != -1) {
                    i += used;
                    continue;
                }
            }
            if (!currTerm.equals("between")){
                addToDic(currTerm, dicOfText, true);
            }
        }
        Set<String> keys = tempEntityDic.keySet();
        for (String key : keys) {
            if (entityDic.contains(key)){

            }
            else if (suspiciousEntityDic.contains(key)){
                suspiciousEntityDic.remove(key);
                entityDic.add(key);
            }
            else{
                suspiciousEntityDic.add(key);
            }
        }
        return tempEntityDic;
    }

    /**
     * parses the words from the query by the given laws and puts them in a hashmap
     * @param wordsInQuery - the hashmap to save the words from the query
     * @param text - the text of the query
     */
    public void parseQuery(HashMap<String, int[]> wordsInQuery, String text){
        if(text == null)
            return;
        String[] terms = text.split(" |\\\n|\\--|\\(|\\)|\\[|\\]|\\)|\\(|\\}|\\{|\\&|\\}|\\:|\\||\\<|\\>|\\?|\\!|\\}|\\_|\\@|\\'\'|\\;|\\\"");
        for(int i = 0; i < terms.length; i++){
            String currTerm = terms[i];
            if (currTerm == null || currTerm.equals("") || currTerm.equals("\n")){
                continue;
            }

            currTerm = deletePunctuations(currTerm);
            terms[i] = currTerm;
            if (currTerm.equals("")){
                continue;
            }
            if (checkIfStopWord(currTerm.toLowerCase())){
                continue;
            }
            String nextTerm = getNextTerm(terms, i);
            String nextNextTerm = getNextTerm(terms, i+1);
            String nextNextNextTerm = getNextTerm(terms, i+2);
            int used = 0;
            if (checkIfNotNumbers(currTerm)){
                used = removeSlash(currTerm, wordsInQuery);
                if (used != -1) {
                    i += used;
                    continue;
                }
                used = castExpressionAndRange(currTerm, nextTerm, nextNextTerm, nextNextNextTerm, wordsInQuery);
                if (used != -1) {
                    i += used;
                    continue;
                }
                used = removeApostrophe(currTerm, wordsInQuery);
                if (used != -1) {
                    i += used;
                    continue;
                }
                used = castDate(currTerm, nextTerm, wordsInQuery);
                if (used != -1) {
                    i += used;
                    continue;
                }
            }
            else{
                used = castExpressionAndRange(currTerm, nextTerm, nextNextTerm, nextNextNextTerm, wordsInQuery);
                if (used != -1) {
                    i += used;
                    continue;
                }
                used = castDate(currTerm, nextTerm, wordsInQuery);
                if (used != -1) {
                    i += used;
                    continue;
                }
                used = castDollar(currTerm, nextTerm, nextNextTerm, nextNextNextTerm, wordsInQuery);
                if (used != -1) {
                    i += used;
                    continue;
                }
                used = castPercent(currTerm, nextTerm, wordsInQuery);
                if (used != -1) {
                    i += used;
                    continue;
                }
                used = castToKMB(currTerm, nextTerm, wordsInQuery);
                if (used != -1) {
                    i += used;
                    continue;
                }
            }
            if (!currTerm.equals("between")){
                addToDic(currTerm, wordsInQuery, true);
            }
        }
    }
    /**
     * getter for suspicious entity dic
     * @return suspicious entity dic (hashset)
     */
    public HashSet<String> getSuspiciousEntityDic  (){
        return suspiciousEntityDic;
    }

    /**
     * creates the object document with all the relevant data
     * @param text - the text of the doc
     * @param docNum - the doc number
     * @param date - the date of the doc
     * @param title - the title of the doc
     * @return - object document
     */
    public Document createDocument (String text, String docNum, String date, String title){
        HashMap<String, int[]> dicOfText = new HashMap<>();
        //dicOfText = parseDoc(text, docNum, date, title, dicOfText);
        HashMap<String, Integer> docEntities = parseDoc(text, docNum, date, title, dicOfText);
        double max_tf = getMaxTF(dicOfText);
        int numOfUniqueWords = getNumOfUniqueWords(dicOfText);
        return new Document (max_tf, numOfUniqueWords, docNum, title, dicOfText, docEntities);
    }

    /**
     * getter for the next term in the array
     * @param terms - string array
     * @param i - the current index
     * @return - the next term
     */
    private String getNextTerm (String[] terms, int i) {
        if (i + 1 >= terms.length)
            return "";
        return terms[i+1];
    }

    /**
     * deletes unwanted punctuations from the beginning and the end of the term
     * @param term - term that we want to delete the punctuations
     * @return - the "clean" term
     */
    private String deletePunctuations (String term){
        if (term == null){
            return "";
        }
        if (term.equals("U.S.")){
            return term;
        }
        while (term.length() > 1 && !Character.isLetter(term.charAt(0)) && !Character.isDigit(term.charAt(0)) && !((term.charAt(0) == '-')) && !((term.charAt(0) == '$'))){
            term = term.substring(1);
        }
        while (term.length() > 1 && !Character.isLetter(term.charAt(term.length()-1)) && !Character.isDigit(term.charAt(term.length()-1)) && !((term.charAt(term.length()-1) == '%'))){
            term = term.substring(0,term.length()-1);
        }
        if (term.length() > 1){
            if (term.charAt(0) == '-' && Character.isLetter(term.charAt(1))){
                term = term.substring(1);
            }
        }
        if (term.length() == 1 && !Character.isLetter(term.charAt(0)) && !Character.isDigit(term.charAt(0))){
            return "";
        }
        return term;
    }

    /**
     * converts number to K/M/B
     * @param currTerm
     * @param nextTerm
     * @param dic
     * @return the number of terms we used (0 means used one word, 1 means used two words)
     */
    private int castToKMB(String currTerm, String nextTerm, HashMap<String, int[]> dic){
        String ans = "";
        int num = -1;
        boolean MinusInBeg =false;
        if (currTerm.contains("/")){
            addToDic(currTerm, dic, false);
            return 0;
        }
        if (onlyDigitDotComma(currTerm)) {
            if (nextTerm.equals("Thousand") || nextTerm.equals("thousand") || nextTerm.equals("k") || nextTerm.equals("K")) {
                ans = currTerm + "K";
                addToDic(ans, dic, false);
                return 1;
            } else if (nextTerm.equals("Million") || nextTerm.equals("million") || nextTerm.equals("m") || nextTerm.equals("M")) {
                ans = currTerm + "M";
                addToDic(ans, dic, false);
                return 1;
            } else if (nextTerm.equals("Billion") || nextTerm.equals("billion") || nextTerm.equals("b") || nextTerm.equals("B")) {
                ans = currTerm + "B";
                addToDic(ans, dic, false);
                return 1;
            }
            else if (checkIfHasComma(currTerm)) {
                currTerm = removeCommas(currTerm);
            }

            int lengthWithoutDotOrMinus = currTerm.length();
            String temp = currTerm;
            if (checkIfHasDot(currTerm)) {
                int indexOfDot = currTerm.indexOf('.');
                temp = currTerm.substring(0, indexOfDot);
                lengthWithoutDotOrMinus = temp.length();
            }
            if (currTerm.charAt(0) == '-') {
                MinusInBeg = true;
                temp = temp.substring(1);
                lengthWithoutDotOrMinus = temp.length();
            }
            if (ans == "" && lengthWithoutDotOrMinus >= 4) { //number larger than 999
                if (checkIfHasDot(currTerm)) {
                    int indexOfDot = currTerm.indexOf('.');
                    currTerm = currTerm.substring(0, indexOfDot);
                }
                if (lengthWithoutDotOrMinus >= 4 && lengthWithoutDotOrMinus <= 6) {
                    num = castToK(temp, dic, MinusInBeg);
                    return num;
                }
                if (lengthWithoutDotOrMinus >= 7 && lengthWithoutDotOrMinus <= 9) {
                    num = castToM(temp, dic, MinusInBeg);
                    return num;
                }
                if (lengthWithoutDotOrMinus >= 10 && lengthWithoutDotOrMinus <= 12) {
                    num = castToB(temp, dic, MinusInBeg);
                    return num;
                }
            }
            else if (ans == "" && lengthWithoutDotOrMinus < 4) { // number smaller than 1,000
                if (checkIfHasSlash(nextTerm)) {
                    ans = currTerm + " " + nextTerm;
                    addToDic(ans, dic, false);
                    return 1;
                } else {
                    if (checkIfHasDot(currTerm)){
                        int indexOfDot = currTerm.indexOf('.');
                        while (currTerm.length()- 1 - indexOfDot > 3){
                            currTerm = currTerm.substring(0, currTerm.length()-1);
                        }
                    }
                    ans = currTerm;
                    addToDic(ans, dic, false);
                    return 0;
                }
            }
        }
        return -1;
    }

    /**
     * checks if the current term has a dot
     * @param term
     * @return true or false
     */
    private boolean checkIfHasDot (String term){
        return term.contains(".");
    }
    /**
     * checks if the current term has a comma
     * @param term
     * @return true or false
     */
    private boolean checkIfHasComma (String term){
        return term.contains(",");
    }
    /**
     * checks if the current term has a slash
     * @param term
     * @return true or false
     */
    private boolean checkIfHasSlash (String term){
        return term.contains("/");
    }

    /**
     * adds M to end of term
     * @param term
     * @param dic
     * @return the number of terms we used (0 means used one word, 1 means used two words)
     */
    private int castToM (String term, HashMap<String, int[]> dic, boolean withMinus) {
        int length = term.length();
        String ans = term;
        if (!term.contains("-")){
            String temp = term;
            ans = temp.substring(0,length-6) + "." + term.substring(length-6,length-3) + "M";
            ans = removeIfLastNumberIsZero(ans);
        }
        if (withMinus){
            ans = "-" + ans;
        }
        addToDic(ans, dic, false);
        return 0;
    }
    /**
     * adds B to end of term
     * @param term
     * @param dic
     * @return the number of terms we used (0 means used one word, 1 means used two words)
     */
    private int castToB (String term, HashMap<String, int[]> dic, boolean withMinus){
        int length = term.length();
        String ans = term;
        if (!term.contains("-")){
            String temp = term;
            ans = temp.substring(0,length-9) + "." + term.substring(length-9,length-6) + "B";
            ans = removeIfLastNumberIsZero(ans);
        }
        if (withMinus){
            ans = "-" + ans;
        }
        addToDic(ans, dic, false);
        return 0;
    }
    /**
     * adds K to end of term
     * @param term
     * @param dic
     * @return the number of terms we used (0 means used one word, 1 means used two words)
     */
    private int castToK (String term, HashMap<String, int[]> dic, boolean withMinus){
        int length = term.length();
        String ans = term;
        if (!term.contains("-")){
            String temp = term;
            ans = temp.substring(0,length-3) + "." + term.substring(length-3) + "K";
            ans = removeIfLastNumberIsZero(ans);
        }
        if (withMinus){
            ans = "-" + ans;
        }
        addToDic(ans, dic, false);
        return 0;
    }

    /**
     * removes commas from the term
     * @param term
     * @return term without commas
     */
    private String removeCommas (String term){
        while (term.contains(",")){
            int indexOfComma = term.indexOf(',');
            term = term.substring(0,indexOfComma) + term.substring(indexOfComma+1);
        }
        return term;
    }

    /**
     * removes unwanted zeros at the end of a term
     * @param term
     * @return term without zeros at the end
     */
    private String removeIfLastNumberIsZero (String term){
        String temp;
        while ((term.charAt(term.length()-2) == '0')){
            temp = term;
            term = temp.substring(0, term.length()-2) + term.charAt(term.length()-1);
        }
        if (term.charAt(term.length()-2) == '.'){
            temp = term;
            term = temp.substring(0, term.length()-2) + term.charAt(term.length()-1);
        }
        return term;
    }

    /**
     * casts numbers that end with % or "percent" or "percentage" to number%
     * @param currTerm
     * @param nextTerm
     * @param dic
     * @return the number of terms we used (0 means used one word, 1 means used two words)
     */
    private int castPercent(String currTerm, String nextTerm, HashMap<String, int[]> dic){
        if(currTerm == null || currTerm.length() <= 0)
            return -1;
        if(currTerm.charAt(currTerm.length()-1) == '%'){
            if(onlyDigitDotComma((currTerm.substring(0,currTerm.length()-1)))){
                addToDic(currTerm, dic, false);
                return 0;
            }
        }
        else if (nextTerm != null && (nextTerm.equals("percent") || nextTerm.equals("percentage"))){
            if (onlyDigitDotComma(currTerm)){
                addToDic(currTerm + "%", dic, false);
                return 1;
            }
        }
        return -1;
    }

    /**
     * checks if the given term contains only numbers, commas, one dot or one slash
     * @param number- to check
     * @return true or false
     */
    private boolean onlyDigitDotComma (String number){
        int dot = 0;
        int slash = 0;
        if (number == null || number.equals(""))
            return false;
        for (Character c: number.toCharArray()){
            if (!Character.isDigit(c) && !(c.equals('.')) && !(c.equals(',')) && !(c.equals('/')) && !(c.equals('-'))){
                return false;
            }
            if (c == '.'){
                dot++;
            }
            if (c == '/'){
                slash++;
            }
            if (dot > 1 || slash > 1){
                return false;
            }
        }
        return true;
    }

    /**
     * casts all terms that start with $ or the second words is Dollars to the format we were told
     * @param currTerm
     * @param nextTerm
     * @param nextNextTerm
     * @param nextNextNextTerm
     * @param dic
     * @return the number of terms we used (0 means used one word, 1 means used two words)
     */
    private int castDollar(String currTerm, String nextTerm, String nextNextTerm, String nextNextNextTerm, HashMap<String, int[]> dic){
        int num = -1;
        String ans;
        if(currTerm == null || currTerm.length() <= 0)
            return -1;
        if (currTerm.contains("'s") && currTerm.charAt(currTerm.length()-1) == 's'){
            currTerm = currTerm.substring(0, currTerm.length()-2);
        }
        if (currTerm.charAt(currTerm.length()-1) == 's'){
            currTerm = currTerm.substring(0, currTerm.length()-1);
        }
        if(currTerm.charAt(0) == '$'){ //$...
            num = startsWith$(currTerm, nextTerm, dic);
            return num;
        }
        else if (nextTerm.equals("Dollars") || nextTerm.equals("dollars")){
            num = nextTermIsDollars(currTerm, nextTerm, dic);
            return num;
        }
        else if(checkIfHasSlash(nextTerm) && (nextNextTerm.equals("Dollars") || nextNextTerm.equals("dollars"))){
            ans = currTerm + " " + nextTerm + " " + nextNextTerm;
            addToDic(ans, dic, false);
            return 2;
        }
        else if ((nextTerm.equals("m") || nextTerm.equals("M") || nextTerm.equals("Bn") || nextTerm.equals("BN") || nextTerm.equals("bn")) && (nextNextTerm.equals("Dollars") || nextNextTerm.equals("dollars"))){
            if(nextTerm.equals("m") || nextTerm.equals("M")){ // 20.6m Dollars
                if(onlyDigitDotComma(currTerm)){
                    ans = currTerm + " M Dollars";
                    addToDic(ans, dic, false);
                    return 1;
                }
            }
            if (nextTerm.equals("bn") || nextTerm.equals("Bn") || nextTerm.equals("BN")){
                if(onlyDigitDotComma(currTerm)){
                    ans = currTerm + "000 M Dollars";
                    addToDic(ans, dic, false);
                    return 1;
                }
            }
        }
        else if ((nextNextTerm.equals("U.S.") || nextNextTerm.equals("u.s.")) && (nextNextNextTerm.equals("dollars")) || nextNextNextTerm.equals("Dollars")){
            if (nextTerm.equals("billion")){
                ans = currTerm + "000 M Dollars";
                addToDic(ans, dic, false);
                return 3;
            }
            else if (nextTerm.equals("million") || nextTerm.equals("Million")){
                ans = currTerm + " M Dollars";
                addToDic(ans, dic, false);
                return 3;
            }
            else if (nextTerm.equals("trillion") || nextTerm.equals("Trillion")){
                ans = currTerm + "000000 M Dollars";
                addToDic(ans, dic, false);
                return 3;
            }
        }
        return num;
    }

    /**
     * casts the term (that contains a dot) to end with dollar with the given format
     * @param currTerm
     * @return
     */
    private String hasDollarAndDot (String currTerm){
        String ans;
        int placeOfDot = currTerm.indexOf(".");
        if (placeOfDot <= 7) {
            ans = currTerm + " Dollars";
        }
        else if (placeOfDot <= 11){
            if (checkIfHasComma(currTerm)){
                int placeOfComma = currTerm.indexOf(',');
                ans = currTerm.substring(0, placeOfComma) + " M Dollars";
            }
            else{
                currTerm = removeZeros(currTerm);
                ans = currTerm + " M Dollars";
            }
        }
        else {
            if (checkIfHasComma(currTerm)){
                int placeOfComma = currTerm.indexOf(',');
                ans = currTerm.substring(0, placeOfComma) + " B Dollars";
            }
            else{
                currTerm = removeZeros(currTerm);
                ans = currTerm + " B Dollars";
            }
        }
        return ans;
    }

    /**
     * removes unwanted zeros up to the last 3 digits
     * @param currTerm
     * @return the changed term
     */
    private String removeZeros (String currTerm){
        if (checkIfHasDot(currTerm)){
            int indexOfDot = currTerm.indexOf(".");
            currTerm = currTerm.substring(0, indexOfDot);
        }
        int length = currTerm.length();
        while (length > 3){
            currTerm = currTerm.substring(0,length-3);
            length = currTerm.length();
            if(currTerm.charAt(length-1) == ','){
                currTerm = currTerm.substring(0,length-1);
            }
            length = currTerm.length();
        }
        return currTerm;
    }

    /**
     * casts terms that start with $ to the given format
     * @param currTerm
     * @param nextTerm
     * @param dic
     * @return the number of terms we used (0 means used one word, 1 means used two words)
     */
    private int startsWith$ (String currTerm, String nextTerm, HashMap<String, int[]> dic){
        String ans;
        currTerm = currTerm.substring(1);
        if(onlyDigitDotComma(currTerm)){
            if (checkIfHasDot(currTerm)) { //$1324.87
                ans = hasDollarAndDot(currTerm);
                addToDic(ans, dic, false);
                return 0;
            }
            else if (checkIfHasSlash(nextTerm)){//$22 3/4
                ans = currTerm + nextTerm + " Dollars";
                addToDic(ans, dic, false);
                return 1;
            }
            else if (nextTerm.equals("million") || nextTerm.equals("Million")){ // $100 million
                ans = currTerm + " M Dollars";
                addToDic(ans, dic, false);
                return 1;
            }
            else if (nextTerm.equals("billion") || nextTerm.equals("Billion")){ // $100 billion
                ans = currTerm + "000 M Dollars";
                addToDic(ans, dic, false);
                return 1;
            }
            else{ //$450,000,000 || $5000
                int length = currTerm.length();
                if (checkIfHasComma(currTerm)){
                    if (length <= 7){
                        ans = currTerm + " Dollars";
                        addToDic(ans, dic, false);
                        return 0;
                    }
                    else if (length <= 11){
                        currTerm = removeZeros(currTerm);
                        ans = currTerm + " M Dollars";
                        addToDic(ans, dic, false);
                        return 0;
                    }
                    else if (length > 11){
                        currTerm = removeZeros(currTerm);
                        ans = currTerm + "000 M Dollars";
                        addToDic(ans, dic, false);
                        return 0;
                    }
                }
                else{
                    if (length <= 6){
                        ans = currTerm + " Dollars";
                        addToDic(ans, dic, false);
                        return 0;
                    }
                    else if (length <= 9){
                        currTerm = removeZeros(currTerm);
                        ans = currTerm + " M Dollars";
                        addToDic(ans, dic, false);
                        return 0;
                    }
                    else if (length > 9){
                        currTerm = removeZeros(currTerm);
                        ans = currTerm + "000 M Dollars";
                        addToDic(ans, dic, false);
                        return 0;
                    }
                }
            }
        }
        return -1;
    }
    /**
     * checks if the next term is dollars and casts it to the given format
     * @param currTerm
     * @param nextTerm
     * @param dic
     * @return the number of terms we used (0 means used one word, 1 means used two words)
     */
    private int nextTermIsDollars (String currTerm, String nextTerm, HashMap<String, int[]> dic){
        String ans;
        if(currTerm.charAt(currTerm.length()-1)=='m' || currTerm.charAt(currTerm.length()-1)=='M'){ // 20.6m Dollars
            currTerm = currTerm.substring(0, currTerm.length()-1);
            if(onlyDigitDotComma(currTerm)){
                ans = currTerm + " M Dollars";
                addToDic(ans, dic, false);
                return 1;
            }
        }
        if (currTerm.length() >= 2 && (currTerm.charAt(currTerm.length()-2)=='b' || currTerm.charAt(currTerm.length()-2)=='B') && (currTerm.charAt(currTerm.length()-1)=='n' || currTerm.charAt(currTerm.length()-1)=='N')){
            currTerm = currTerm.substring(0, currTerm.length()-2);
            if(onlyDigitDotComma(currTerm)){
                ans = currTerm + "000 M Dollars";
                addToDic(ans, dic, false);
                return 1;
            }
        }
        else if(onlyDigitDotComma(currTerm)){
            if (checkIfHasDot(currTerm)) { //1324.87 Dollars
                ans = hasDollarAndDot(currTerm);
                addToDic(ans, dic, false);
                return 1;
            }
            else{ //450,000,000 Dollars || 5000 Dollars
                int length = currTerm.length();
                if (checkIfHasComma(currTerm)){
                    if (length <= 7){
                        ans = currTerm + " Dollars";
                        addToDic(ans, dic, false);
                        return 1;
                    }
                    else if (length <= 11){
                        currTerm = removeZeros(currTerm);
                        ans = currTerm + " M Dollars";
                        addToDic(ans, dic, false);
                        return 1;
                    }
                    else if (length > 11){
                        currTerm = removeZeros(currTerm);
                        ans = currTerm + "000 M Dollars";
                        addToDic(ans, dic, false);
                        return 1;
                    }
                }
                else{
                    if (length <= 6){
                        ans = currTerm + " Dollars";
                        addToDic(ans, dic, false);
                        return 1;
                    }
                    else if (length <= 9){
                        currTerm = removeZeros(currTerm);
                        ans = currTerm + " M Dollars";
                        addToDic(ans, dic, false);
                        return 1;
                    }
                    else if (length > 9){
                        currTerm = removeZeros(currTerm);
                        ans = currTerm + "000 M Dollars";
                        addToDic(ans, dic, false);
                        return 1;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * casts dates to the given format
     * @param currTerm
     * @param nextTerm
     * @param dic
     * @return the number of terms we used (0 means used one word, 1 means used two words)
     */
    private int castDate (String currTerm, String nextTerm, HashMap<String, int[]> dic){
        String ans;
        String date = checkIfDateLegal(currTerm);
        if (months.containsKey(nextTerm) && (date != "")){
            ans = months.get(nextTerm) + "-" + checkIfDateLegal(currTerm);
            addToDic(ans, dic, false);
            return 1;
        }
        if (months.containsKey(currTerm) && (checkIfDateLegal(nextTerm) != "")){
            ans = months.get(currTerm) + "-" + checkIfDateLegal(nextTerm);
            addToDic(ans, dic, false);
            return 1;
        }
        if (months.containsKey(currTerm) && checkIfYearLegal(nextTerm) != ""){
                ans = nextTerm + "-" + months.get(currTerm);
                addToDic(ans, dic, false);
                return 1;
        }
        return -1;
    }

    /**
     * checks if the given term contains only numbers and if is a single digit casts to double digit
     * @param currTerm
     * @return the term or ""
     */
    private String checkIfDateLegal (String currTerm){
        String ans = "";
        if (currTerm.length() <= 2){
            for (Character c: currTerm.toCharArray()) {
                if (!Character.isDigit(c)) {
                    return "";
                }
            }
            if (currTerm.length() == 1){
                return "0" + currTerm;
            }
            return currTerm;
        }
        return "";
    }
    /**
     * checks if the given term contains only numbers and it length equals 4
     * @param currTerm
     * @return the term or ""
     */
    private String checkIfYearLegal (String currTerm){
        if (currTerm.length() == 4){
            for (Character c: currTerm.toCharArray()) {
                if (!Character.isDigit(c)) {
                    return "";
                }
            }
            return currTerm;
        }
        return "";
    }

    /**
     * checks if the given term contains only letters
     * @param currTerm
     * @return true or false
     */
    private boolean checkIfOnlyLetters (String currTerm){
        if (currTerm.length() > 1){
            if (Character.isLetter(currTerm.charAt(0)) && Character.isLetter(currTerm.charAt(currTerm.length()-1))){
                return true;
            }
        }else if (!currTerm.equals("") && Character.isLetter(currTerm.charAt(0))){
            return true;
        }
        return false;
    }

    /**
     * checks if the given term is not a number
     * @param currTerm
     * @return true or false
     */
    private boolean checkIfNotNumbers (String currTerm){
        if (currTerm.charAt(0) == '-' || currTerm.charAt(currTerm.length()-1) == '%' || currTerm.charAt(0) == '$'){
            return false;
        }
        if (!Character.isDigit(currTerm.charAt(0)) && !Character.isDigit(currTerm.charAt(currTerm.length()-1))){
            return true;
        }
        return false;
    }

    /**
     * checks if term contains a dash and saves by the given format
     * @param currTerm
     * @param nextTerm
     * @param nextNextTerm
     * @param nextNextNextTerm
     * @param dic
     * @return the number of terms we used (0 means used one word, 1 means used two words)
     */
    private int castExpressionAndRange (String currTerm, String nextTerm, String nextNextTerm, String nextNextNextTerm, HashMap<String, int[]> dic){
        String[] parts;
        if (currTerm.contains("-")){
            parts = currTerm.split("-");
            if (parts.length == 2){
                if (onlyDigitDotComma(parts[0]) && onlyDigitDotComma(parts[1])){
                    addToDic(currTerm, dic, false);
                    addToDic(parts[0], dic, false);
                    if (parts[1].charAt(0) == ','){
                        parts[1] = parts[1].substring(1);
                    }
                    addToDic(parts[1], dic, false);
                    return 0;
                }
                else if ((onlyDigitDotComma(parts[0]) && checkIfOnlyLetters(parts[1])) ||(onlyDigitDotComma(parts[1]) && checkIfOnlyLetters(parts[0])) || (checkIfOnlyLetters(parts[0]) && checkIfOnlyLetters(parts[1]))){
                    if (checkIfOnlyLetters(parts[0])){
                        if (parts[0].charAt(0) >= 'A' && parts[0].charAt(0)<= 'Z' ){
                            if(!dic.containsKey(currTerm.toLowerCase())){
                                addToDic(currTerm.toUpperCase(), dic, false);
                            }else{
                                addToDic(currTerm.toLowerCase(), dic, false);
                            }
                        }
                        else if (parts[0].charAt(0) >= 'a' && parts[0].charAt(0)<= 'z'){
                            if(dic.containsKey(currTerm.toUpperCase())){
                                int[] valueOfCurrTerm = dic.get(currTerm.toUpperCase());
                                valueOfCurrTerm[0]++;
                                dic.put(currTerm.toLowerCase(), valueOfCurrTerm);
                                dic.remove(currTerm.toUpperCase());
                            }
                            else{
                                addToDic(currTerm.toLowerCase(), dic, false);
                            }
                        }
                    }
                    else if (checkIfOnlyLetters(parts[1])){
                        if (parts[1].charAt(0) >= 'A' && parts[1].charAt(0)<= 'Z' ){
                            if(!dic.containsKey(currTerm.toLowerCase())){
                                addToDic(currTerm.toUpperCase(), dic, false);
                            }else{
                                addToDic(currTerm.toLowerCase(), dic, false);
                            }
                        }
                        else if (parts[1].charAt(0) >= 'a' && parts[1].charAt(0)<= 'z'){
                            if(dic.containsKey(currTerm.toUpperCase())){
                                int[] valueOfCurrTerm = dic.get(currTerm.toUpperCase());
                                valueOfCurrTerm[0]++;
                                dic.put(currTerm.toLowerCase(), valueOfCurrTerm);
                                dic.remove(currTerm.toUpperCase());
                            }
                            else{
                                addToDic(currTerm.toLowerCase(), dic, false);
                            }
                        }
                    }
                    return 0;
                }
            }
            else if (parts.length > 2){
                for (int i = 0; i < parts.length-1; i++){
                    if (!checkIfOnlyLetters(parts[i])) {
                        return -1;
                    }
                }
                if (parts[0].charAt(0) >= 'A' && parts[0].charAt(0)<= 'Z' ){
                    if(!dic.containsKey(currTerm.toLowerCase())){
                        addToDic(currTerm.toUpperCase(), dic, false);
                    }else{
                        addToDic(currTerm.toLowerCase(), dic, false);
                    }
                }
                else if (parts[0].charAt(0) >= 'a' && parts[0].charAt(0)<= 'z'){
                    if(dic.containsKey(currTerm.toUpperCase())){
                        int[] valueOfCurrTerm = dic.get(currTerm.toUpperCase());
                        valueOfCurrTerm[0]++;
                        dic.put(currTerm.toLowerCase(), valueOfCurrTerm);
                        dic.remove(currTerm.toUpperCase());
                    }
                    else{
                        addToDic(currTerm.toLowerCase(), dic, false);
                    }
                }
                return 0;
            }
        }
        else if ((currTerm.equals("between") || currTerm.equals("Between")) && onlyDigitDotComma(nextTerm) && nextNextTerm.equals("and") && onlyDigitDotComma(nextNextNextTerm)){
            addToDic(nextTerm, dic, false);
            addToDic(nextNextNextTerm, dic, false);
            addToDic(nextTerm + "-" + nextNextNextTerm, dic, false);
            return 3;
        }
        return -1;
    }

    /**
     * one of the laws we added. removes 's from end of term
     * @param currTerm
     * @param dic
     * @return the number of terms we used (0 means used one word, 1 means used two words)
     */
    private int removeApostrophe(String currTerm, HashMap<String, int[]> dic){
        String[] parts;
        if (currTerm.contains("'s") && currTerm.charAt(currTerm.length()-1) == 's'){
            parts = currTerm.split("'s");
            if (checkIfOnlyLetters(parts[0])){
                addToDic(parts[0], dic, true);
                return 0;
            }
        }
        else if (currTerm.contains("'S") && currTerm.charAt(currTerm.length()-1) == 'S'){
            parts = currTerm.split("'S");
            if (checkIfOnlyLetters(parts[0])){
                addToDic(parts[0], dic, true);
                return 0;
            }
        }
        return -1;
    }

    /**
     * one of the laws we added. splits terms that contain a / in them.
     * @param currTerm
     * @param dic
     * @return the number of terms we used (0 means used one word, 1 means used two words)
     */
    private int removeSlash(String currTerm, HashMap<String, int[]> dic){
        String[] parts;
        while (currTerm.contains("/")){
            parts = currTerm.split("/");
            for(int j = 0 ; j < parts.length ; j++){
                if (checkIfOnlyLetters(parts[j])){
                    addToDic(parts[j], dic, true);
                }
            }
            return 0;
        }
        return -1;
    }

    /**
     * checks how the term appears in the dictionary (if it appears) and returns the term in that same way
     * @param currTerm
     * @param dic
     * @return the changed term
     */
    private String checkFirstLetter (String currTerm, HashMap<String, int[]> dic){
        if (currTerm != null && currTerm != "") {
            if (currTerm.charAt(0) >= 'A' && currTerm.charAt(0)<= 'Z'){
                if(!dic.containsKey(currTerm.toLowerCase())){
                    return currTerm.toUpperCase();
                }
                else{
                    return currTerm.toLowerCase();
                }
            }
            else if (currTerm.charAt(0) >= 'a' && currTerm.charAt(0)<= 'z'){
                if(dic.containsKey(currTerm.toUpperCase())){
                    int[] valueOfCurrTerm = dic.get(currTerm.toUpperCase());
                    valueOfCurrTerm[0]++;
                    dic.put(currTerm.toLowerCase(), valueOfCurrTerm);
                    dic.remove(currTerm.toUpperCase());
                    return "";
                }
                else{
                    return currTerm.toLowerCase();
                }
            }
        }
        return currTerm;
    }

    /**
     * add terms to the dictionary. if the term is already in the dictionary we add one to the termCounter
     * @param currTerm
     * @param dic
     * @param toStem
     */
    private void addToDic (String currTerm, HashMap<String, int[]> dic, boolean toStem){
        if (currTerm != null && currTerm.length() > 0 && !checkIfStopWord(currTerm.toLowerCase())){
            if (stem && toStem){
                stemmer.setStem(currTerm);
                stemmer.stem();
                currTerm = stemmer.getStem();
            }
            if (Character.isLetter(currTerm.charAt(0))){
                String changedTerm = checkFirstLetter(currTerm, dic);
                if (!changedTerm.equals("")){
                    currTerm = changedTerm;

                }
                else{
                    return;
                }
            }
            if (dic.containsKey(currTerm)){
                int[] valueOfCurrTerm = dic.get(currTerm);
                valueOfCurrTerm[0]++;
                dic.put(currTerm, valueOfCurrTerm);
            }
            else{
                int[] valueOfTerm = new int[2];
                valueOfTerm[0] = 1;//tf (term frequency)- number of times the term is seen
                valueOfTerm[1] = 1;//df (document frequency)- number of Documents the term is in
                dic.put(currTerm, valueOfTerm);
            }
        }
    }

    /**
     * inserts all the stop words from stop words file into HashSet
     * scanner function from link: "https://stackoverflow.com/questions/30011400/splitting-textfile-at-whitespace"
     */
    private void insertStopWordsIntoHashSet (){
        String path = stopWordsPath + "\\stop_words.txt";
        File file = new File(path);
        String fileString = castFileToString(file);
        if(!fileString.equals("")) {
            String[] currStopWord = fileString.split("\r\n|\\\n");
            for (String str : currStopWord)
                stopWords.add(str);
        }
    }
    /**
     * the function check if a word is a stop word or not from stopWords HastSet.
     * @param currTerm
     * @return true or false
     */
    private boolean checkIfStopWord (String currTerm) {
        if ((stopWords.contains(currTerm) && !currTerm.equals("between")))
            return true;
        return false;
    }
    /**
     * from url: https://howtodoinjava.com/java/io/java-read-file-to-string-examples/
     * read file to String line by line
     * @param file - The path of the file to read into String
     * @return - String of the context of the file
     */
    private String castFileToString(File file)
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
    /**
     * finds the max tf using an iterator over the dicDoc hashmap
     * Link : https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
     */
    private int getMaxTF(HashMap<String,int[]> dic){
        int max = 0;
        Iterator it = dic.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            int []temp = (int[])pair.getValue();
            if(temp[0] > max)
                max = temp[0];
        }
        return max;
    }
    /**
     * return the size of dicDoc - the number of unique words from current dictionary of document.
     */
    private int getNumOfUniqueWords(HashMap<String, int[]> dic){
        return dic.size();
    }

    /**
     * getter for entityDic
     * @return - entitydic
     */
    public HashSet<String> getEntityDic(){
        return entityDic;
    }

}
