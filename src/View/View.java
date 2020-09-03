package View;

import Controller.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class View {
    @FXML
    private TextField corpusPath;
    @FXML
    private TextField savedFilesPath;
    @FXML
    private Button BrowseFrom;
    @FXML
    private Button BrowseTo;
    @FXML
    private TextField queryText;
    @FXML
    private CheckBox stemming;
    @FXML
    private Button display;
    @FXML
    private Button load;
    @FXML
    private Button reset;
    @FXML
    private CheckBox onlineSemantic;
    @FXML
    private CheckBox offlineSemantic;
    @FXML
    private Button runQuery;
    @FXML
    private Button RunQueryFile;
    @FXML
    private TextField QueryFileText;
    @FXML
    private Button BrowseQueryFile;
    @FXML
    private TextField pathResults;
    @FXML
    private Button BrowseQueryPath;
    @FXML
    private CheckBox saveResults;

    private Controller controller = new Controller();


    @FXML
    private void Commit(ActionEvent event) throws IOException {
        boolean partA = true;
        String corpus = corpusPath.getText();
        String savedFiles = savedFilesPath.getText();
        if(corpus == null || corpus.equals("Please select a folder.") || corpus.equals("")) {
            if (postingsExist(savedFiles)){
                reset.setDisable(false);
                display.setDisable(false);
                load.setDisable(false);
                partA = false;
            }

        }
        if(savedFiles == null || savedFiles.equals("No Directory selected") || savedFiles.equals("")) {
            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
            chooseFile.setHeaderText("ERROR!");
            chooseFile.setContentText("Please choose a legal folder you would like to save the files to.");
            chooseFile.show();
            return;
        }
        boolean succeeded = true;
        if (partA){
            succeeded = controller.Commit(corpus , savedFiles, stemming.isSelected());
        }
        if(succeeded) {
            reset.setDisable(false);
            load.setDisable(false);
            display.setDisable(false);
        }

    }
    private boolean postingsExist (String savedFiles) {
        String pathName1 = "";
        String pathName2 = "";
        String pathName3 = "";
        if (stemming.isSelected()) {
            pathName1 = savedFiles + "\\WithStemming\\ExtraData";
            File resultsFile1 = new File(pathName1);
            pathName2 = savedFiles + "\\WithStemming\\Postings";
            File resultsFile2 = new File(pathName2);
            pathName3 = savedFiles + "\\stop_words.txt";
            File resultsFile3 = new File(pathName3);
            if (resultsFile1.exists() && resultsFile2.exists() && resultsFile3.exists()) {
                return true;
            } else {
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("ERROR!");
                chooseFile.setContentText("Please choose a legal folder that contains the ExtraData, Postings and stop words file.");
                chooseFile.show();
                return false;
            }
        } else {
            pathName1 = savedFiles + "\\WithoutStemming\\ExtraData";
            File resultsFile1 = new File(pathName1);
            pathName2 = savedFiles + "\\WithoutStemming\\Postings";
            File resultsFile2 = new File(pathName2);
            pathName3 = savedFiles + "\\stop_words.txt";
            File resultsFile3 = new File(pathName3);
            if (resultsFile1.exists() && resultsFile2.exists() && resultsFile3.exists()) {
                return true;
            } else {
                Alert chooseFile = new Alert(Alert.AlertType.ERROR);
                chooseFile.setHeaderText("ERROR!");
                chooseFile.setContentText("Please choose a legal folder that contains the ExtraData, Postings and stop words file.");
                chooseFile.show();
                return false;
            }
        }
    }
    private void showAlert(Alert.AlertType type, String header, String context) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(context);
        alert.showAndWait();
    }

    @FXML
    private void browseFrom(ActionEvent event) throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = new Stage();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory == null) {
            corpusPath.setText("Please select a folder.");
        } else {
            corpusPath.setText(selectedDirectory.getAbsolutePath());
        }
    }
    @FXML
    private void browseResults(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory == null) {
            pathResults.setText("Please select a folder.");
        } else {
            pathResults.setText(selectedDirectory.getAbsolutePath());
        }
    }
    @FXML
    private void BrowseQueryFile(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            QueryFileText.setText("Please select a folder.");
        } else {
            QueryFileText.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void browseTo(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory == null) {
            savedFilesPath.setText("Please select a folder.");
        } else {
            savedFilesPath.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void Reset(ActionEvent event) throws IOException {
        if(postingsExist(savedFilesPath.getText())) {
            boolean succeeded = controller.Reset();
            if (succeeded) {
                showAlert(Alert.AlertType.INFORMATION, "RESET", "The system has been reset.");
                display.setDisable(true);
                load.setDisable(false);
                reset.setDisable(true);
                runQuery.setDisable(true);
                BrowseQueryFile.setDisable(true);
                onlineSemantic.setDisable(true);
                offlineSemantic.setDisable(true);
                BrowseQueryPath.setDisable(true);
                RunQueryFile.setDisable(true);
                saveResults.setDisable(true);
            } else {
                showAlert(Alert.AlertType.ERROR, "RESET", "The reset failed. Please try again.");
            }
        }
        else{
            showAlert(Alert.AlertType.INFORMATION, "CAN'T RESET", "There isn't anything to reset.");
        }
    }

    @FXML
    private void LoadDictionary(ActionEvent event) throws IOException {
        if (savedFilesPath.getText() == null || savedFilesPath.getText().equals("")){
            showAlert(Alert.AlertType.ERROR, "LOAD","Please first insert the path to the postings.");
        }
        else if (postingsExist(savedFilesPath.getText())) {
            boolean succeeded = controller.LoadDictionary(stemming.isSelected(), savedFilesPath.getText());
            if (succeeded) {
                showAlert(Alert.AlertType.INFORMATION, "LOAD", "The dictionary has loaded successfully.");
                runQuery.setDisable(false);
                RunQueryFile.setDisable(false);
                BrowseQueryFile.setDisable(false);
                BrowseQueryPath.setDisable(false);
                reset.setDisable(false);
                display.setDisable(false);
                onlineSemantic.setDisable(false);
                offlineSemantic.setDisable(false);
                saveResults.setDisable(false);
            } else {
                showAlert(Alert.AlertType.ERROR, "LOAD", "The dictionary wasn't able to load. Please try again.");
            }
        }
    }

    @FXML
    private void DisplayDictionary(ActionEvent event) throws IOException {
        List<String> dictionary = controller.DisplayDictionary(stemming.isSelected() ,savedFilesPath.getText());
        if(dictionary == null){
            showAlert(Alert.AlertType.ERROR, "DISPLAY","We weren't able to display the dictionary. Please try again.");
        }
        else{
            ListView<String> listView = new ListView<>();
            listView.getItems().setAll(FXCollections.observableList(dictionary));
            Stage stage = new Stage();
            AnchorPane anchorPane = new AnchorPane();
            anchorPane.getChildren().addAll(listView);
            AnchorPane.setRightAnchor(listView,0.0);
            AnchorPane.setLeftAnchor(listView,0.0);
            anchorPane.setPrefWidth(500.0);
            anchorPane.setPrefHeight(400.0);
            Scene scene = new Scene(anchorPane,500,400);
            stage.setScene(scene);
            stage.show();
        }

    }
    @FXML
    private void runQueryFromUser(ActionEvent event) throws IOException {
        //  if(postingsExist(savedFilesPath.getText())){
        HashMap<String,Double> docsAndIsRank = controller.runQueryFromUser(queryText.getText(),stemming.isSelected(), savedFilesPath.getText(), corpusPath.getText(), onlineSemantic.isSelected(), offlineSemantic.isSelected(),saveResults.isSelected(),pathResults.getText());
        final ObservableList<MyData> queriesAndDocs = FXCollections.observableArrayList();
        Set<String> keys = docsAndIsRank.keySet();
        for (String doc : keys) {
            queriesAndDocs.add(new MyData("100",doc));
        }
        tableViewQueries(queriesAndDocs);
        // }
//        else if(corpusPath.getText() == null || corpusPath.getText().equals("")) {
//            Alert chooseFile = new Alert(Alert.AlertType.ERROR);
//            chooseFile.setHeaderText("ERROR");
//            chooseFile.setContentText("The folder you selected does not contain the corpus.");
//            chooseFile.show();
//
//        }

    }
    @FXML
    private void runQueryFile(ActionEvent event) throws IOException {

        HashMap<String, HashMap<String, Double>> allTheRankedDocs = controller.runQueryFromFile(QueryFileText.getText(),stemming.isSelected(),corpusPath.getText(),savedFilesPath.getText(),onlineSemantic.isSelected(), offlineSemantic.isSelected() ,saveResults.isSelected(),pathResults.getText());
        List<Map.Entry<String, HashMap<String, Double>>> list = new LinkedList<>(allTheRankedDocs.entrySet());
        final ObservableList<MyData> docsAndQueries = FXCollections.observableArrayList();
        for (Map.Entry<String, HashMap<String, Double>> entry : list) {
            HashMap<String,Double> docsAndRateOfCurrQuery = allTheRankedDocs.get(entry.getKey());
            Set<String> docs = docsAndRateOfCurrQuery.keySet();
            for (String doc:docs) {
                docsAndQueries.add(new MyData(entry.getKey(),doc));
            }
        }

        Set<String> keys = allTheRankedDocs.keySet();
        for (String query : keys) {
            HashMap<String,Double> docsAndRateOfCurrQuery=allTheRankedDocs.get(query);
            Set<String> docs = docsAndRateOfCurrQuery.keySet();
            for (String doc:docs) {
                docsAndQueries.add(new MyData(query,doc));
            }
        }
        Comparator<MyData> compareData = new Comparator<MyData>() {
            @Override
            public int compare(MyData s1, MyData s2) {
                String n1 = s1.getQueryID();
                String n2 = s2.getQueryID();
                return n1.compareToIgnoreCase(n2);
            }
        };
        FXCollections.sort(docsAndQueries,compareData);
        //docsAndQueries.sort((str, str2) -> MyDataType.compareToIgnoreCase(str, str2));

        tableViewQueries(docsAndQueries);
    }
    private void tableViewQueries(ObservableList<MyData> queriesAndDocs){
        TableView<MyData> tableView = new TableView<MyData>();
        TableColumn queryCol = new TableColumn("Query ID");
        queryCol.setCellValueFactory(new PropertyValueFactory<MyData,String>("queryID"));
        TableColumn docCol = new TableColumn("Doc ID");
        docCol.setCellValueFactory(new PropertyValueFactory<MyData,String>("docID"));

        //***********************
        TableColumn entitiesCol = new TableColumn("Entities");
        //entitiesCol.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));

        Callback<TableColumn<MyData, String>, TableCell<MyData, String>> cellFactory =
                new Callback<TableColumn<MyData, String>, TableCell<MyData, String>>() {
                    @Override
                    public TableCell call(final TableColumn<MyData, String> param) {
                        final TableCell<MyData, String> cell = new TableCell<MyData, String>() {
                            final Button btn = new Button("Show entities");
                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    btn.setOnAction(event -> {
                                        MyData myDataType = getTableView().getItems().get(getIndex());
                                        String docToShowEntities = myDataType.getDocID();
                                        List<String> fiveEntities = controller.getEntities(docToShowEntities);
                                        //List<Map.Entry<String, Double>> list = new LinkedList<>(entities.entrySet());
                                        /*Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
                                            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                                                return (o2.getValue()).compareTo(o1.getValue());
                                            }
                                        });
                                        List<String> fiveEntitiesDocs = new LinkedList<>();
                                        int docLimit = Math.min(5, list.size());
                                        for (int i = 0; i < docLimit; i++) {
                                            Map.Entry docPair = list.get(i);
                                            fiveEntitiesDocs.add((String) docPair.getKey());
                                        }*/
                                        ListView<String> listView = new ListView<>();
                                        listView.getItems().setAll(FXCollections.observableList(fiveEntities));
                                        Stage stage = new Stage();
                                        AnchorPane anchorPane = new AnchorPane();
                                        anchorPane.getChildren().addAll(listView);
                                        AnchorPane.setRightAnchor(listView,0.0);
                                        AnchorPane.setLeftAnchor(listView,0.0);
                                        anchorPane.setPrefWidth(500.0);
                                        anchorPane.setPrefHeight(400.0);
                                        Scene scene = new Scene(anchorPane,500,400);
                                        stage.setScene(scene);
                                        stage.show();

                                    });
                                    setGraphic(btn);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                };
        entitiesCol.setCellFactory(cellFactory);
        tableView.setItems(queriesAndDocs);
        tableView.getColumns().addAll(queryCol,docCol,entitiesCol);

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new javafx.geometry.Insets(7, 0, 0, 7));
        vbox.getChildren().addAll(tableView);
        Stage stage = new Stage();
        Scene scene = new Scene(vbox,500,400);
        stage.setScene(scene);
        stage.show();
    }
    public class MyData {
        private final String queryID ;
        private final String docID ;
        public MyData(String queryID, String docID) {
            this.queryID = queryID;
            this.docID = docID;
        }
        public String getQueryID() {
            return queryID ;
        }
        public String getDocID() {
            return docID ;
        }
    }
}
