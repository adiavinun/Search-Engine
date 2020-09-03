package Model;

import java.util.*;

public class Ranker {
    // all the relevant docs for the query
    private HashMap<String, Document> docs;
    // the average length of the docs
    private double averageDocLength;

    Ranker(HashMap<String, Document> docs, double averageDocLength){
        this.docs = docs;
        this.averageDocLength = averageDocLength;
    }

    /**
     * ranks all the relevant docs from the query
     * @param allTheQueryWords - all the word from the query
     * @param docsID - all the relevant docs
     * @return the ranked docs
     */
    public HashMap<String, Double> rankAllDocs(HashMap<String, QueryWord> allTheQueryWords, HashSet<String> docsID){
        ArrayList<HashMap<String, Double>> docsRanks = new ArrayList<>();
        HashMap<String, Double> bm25Rank = BM25(allTheQueryWords, docsID);
        docsRanks.add(bm25Rank);
        HashMap<String, Double> docTitleRank = rankIfQWordAppearsInDocTitle(allTheQueryWords, docsID);
        docsRanks.add(docTitleRank);
        HashMap<String, Double> queryRank = rankIfQWordAppearsInTitleDescNarr(allTheQueryWords, docsID);
        docsRanks.add(queryRank);
        return combineAllRanks(docsRanks);
    }

    /**
     * ranks the docs using the BM25 algorithm
     * @param allTheQueryWords - all the words from the current query
     * @param docsID - the docIds of all the relevant docs
     * @return - hashmap of all the ranked docs
     */
    private HashMap<String, Double> BM25(HashMap<String, QueryWord> allTheQueryWords, HashSet<String> docsID) {
        double k = 1.2;
        double b = 0.75;
        int N = docs.size();
        //best match doc will be the first, second be the after him.....
        HashMap<String, Double> docsWithScore = new HashMap<>();
        //foreach word from query (sigma)
        Set set = allTheQueryWords.entrySet();
        Iterator it = set.iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            QueryWord wordFromQuery = (QueryWord) pair.getValue();
            HashMap<String, Integer> tfPerDoc = wordFromQuery.getTfPerDoc();
            if (tfPerDoc == null || tfPerDoc.size() == 0)
                continue;
            //foreach doc
            Set<String> keys = tfPerDoc.keySet();
            for (String docID : keys) {
                //n(qi)
                int df = wordFromQuery.getDf();
                //IDF(qi)
                double idf = Math.log10((N-df + 0.5) / (df + 0.5));
                //f(qi,D)
                int tf = tfPerDoc.get(docID);
                Document currDoc = docs.get(docID);
                //|D|
                double docLength = currDoc.getDocLength();
                double mone = (idf * tf * (k+1));
                double mechane = (tf + k * (1 - b + b * (docLength/averageDocLength)));
                double rankOfDocQuery = mone / mechane;
                if (docsWithScore.containsKey(docID)) {
                    double tempRank = docsWithScore.get(docID);
                    rankOfDocQuery += tempRank;
                    docsWithScore.put(docID, rankOfDocQuery);
                }
                else docsWithScore.put(docID, rankOfDocQuery);
            }
        }
        return docsWithScore;
    }

    /**
     * rank the docs if the word appears in the docs title
     * @param allTheQueryWords - all the words from the current query
     * @param docsID - the docIds of all the relevant docs
     * @return - hashmap of all the ranked docs
     */
    private HashMap<String, Double> rankIfQWordAppearsInDocTitle(HashMap<String, QueryWord> allTheQueryWords, HashSet<String> docsID) {
        HashMap<String, Double> docsWithScore = new HashMap<>();
        if (allTheQueryWords == null)
            return docsWithScore;
        Set set = allTheQueryWords.entrySet();
        Iterator it = set.iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            QueryWord wordFromQuery = (QueryWord) pair.getValue();
            HashMap<String, Integer> docsPerWord = wordFromQuery.getTfPerDoc();
            Set<String> keys = docsPerWord.keySet();
            for (String docID : keys) {
                Document doc = docs.get(docID);
                String title = doc.getTitle();
                if (title.contains(wordFromQuery.getWord())){
                    if (docsWithScore.containsKey(docID)) {
                        docsWithScore.put(docID, docsWithScore.get(docID) + 1);
                    } else{
                        docsWithScore.put(docID, 1.0);
                    }
                }
                else{
                    if (!docsWithScore.containsKey(docID)) {
                        docsWithScore.put(docID, 0.0);
                    }
                }
            }
        }
        return docsWithScore;
    }

    /**
     * rank the docs if the word appears in the querys title, desc and/or narr
     * @param allTheQueryWords - all the words from the current query
     * @param docsID - the docIds of all the relevant docs
     * @return - hashmap of all the ranked docs
     */
    private HashMap<String, Double> rankIfQWordAppearsInTitleDescNarr (HashMap<String, QueryWord> allTheQueryWords, HashSet<String> docsID) {
        HashMap<String, Double> docsWithScore = new HashMap<>();
        if (allTheQueryWords == null)
            return docsWithScore;
        Set set = allTheQueryWords.entrySet();
        Iterator it = set.iterator();
        double titleRank;
        double descRank;
        double narrRank;
        while (it.hasNext()) {
            titleRank = 0;
            descRank = 0;
            narrRank = 0;
            Map.Entry pair = (Map.Entry) it.next();
            QueryWord wordFromQuery = (QueryWord) pair.getValue();
            HashMap<String, Integer> docsPerWord = wordFromQuery.getTfPerDoc();
            Set<String> keys = docsPerWord.keySet();
            if (wordFromQuery.getIsInTitle()) {
                titleRank = 1;
            }
            if (wordFromQuery.getIsInDesc()) {
                descRank = 0.7;
            }
            if (wordFromQuery.getIsInNarr()) {
                narrRank = 0.3;
            }
            for (String docID : keys) {
                if (docsWithScore.containsKey(docID)) {
                    docsWithScore.put(docID, docsWithScore.get(docID) + titleRank + descRank + narrRank);
                } else {
                    docsWithScore.put(docID, titleRank + descRank + narrRank);
                }
            }
        }
        return docsWithScore;
    }

    /**
     * combines all the ranks of the docs by the given weights to one final rank for each doc
     * @param docsRanks - list of the ranked docs of all the queries
     * @return hashmap - the final rates of the docs
     */
    private HashMap<String, Double> combineAllRanks(ArrayList<HashMap<String, Double>> docsRanks){
        HashMap<String, Double> finalRank = new HashMap<>();
        double[] weights = new double[docsRanks.size()];
        weights[0] = 0.6;//bm25
        weights[1] = 0.1;//docTitle
        weights[2] = 0.3;//queryRank
        for (int j = 0; j < docsRanks.size(); j++) {
            HashMap<String, Double> rankedDoc = docsRanks.get(j);
            Set set = rankedDoc.entrySet();
            Iterator it = set.iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String docID = (String) pair.getKey();
                double docRank = (double) pair.getValue();
                if (finalRank.containsKey(docID)) {
                    finalRank.put(docID, finalRank.get(docID) + (docRank * weights[j]));
                } else {
                    finalRank.put(docID, docRank * weights[j]);
                }
            }
        }
        return finalRank;
    }
    public List<String> getEntities(String docID){
        Document document = docs.get(docID);
        return document.getHasMapTopFiveEntities();
    }
}
