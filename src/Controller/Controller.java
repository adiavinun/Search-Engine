package Controller;

import Model.Model;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Controller {
    private Model model = new Model();

    public boolean Commit(String corpusPath, String savedFilesPath, boolean stem) {
        return model.generateInvertedIndex(corpusPath , savedFilesPath, stem);
    }

    public boolean Reset() {
        return model.reset();
    }

    public boolean LoadDictionary(boolean stem, String savedFilesPath) {
        return model.loadDictionaryFromDiskToMemory(stem ,savedFilesPath);
    }

    public List<String> DisplayDictionary(boolean stem, String savedFilesPath) {
        return model.displayDictionary(stem, savedFilesPath);
    }
    public List<String> getEntities(String docID){
        return model.getEntities(docID);
    }
    public HashMap<String,Double> runQueryFromUser(String query, boolean toStem, String savedFilesPath, String corpusPath, boolean onlineSemantic, boolean offlineSemantic, boolean toSaveResults, String pathForResults) {
        return model.runQueryFromUser(query, toStem , savedFilesPath, corpusPath, onlineSemantic, offlineSemantic, toSaveResults, pathForResults);
    }
    public HashMap<String, HashMap<String, Double>> runQueryFromFile(String pathQueryFile, boolean toStem, String corpusPath, String savedFilesPath,boolean onlineSemantic, boolean offlineSemantic, boolean toSaveResults, String pathForResults) {
        try {
            return model.runQueryFromFile(pathQueryFile, toStem, corpusPath, savedFilesPath, onlineSemantic, offlineSemantic, toSaveResults, pathForResults);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
