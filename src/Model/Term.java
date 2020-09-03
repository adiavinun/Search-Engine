package Model;

import java.util.ArrayList;

public class Term {

    private String name;
    private int df;
    private int tf;
    private ArrayList<String> listOfDocs;

    public Term(String name){
        this.name = name;
        df = 1;
        tf = 1;
        listOfDocs = new ArrayList<>();
    }

    public String getName(){
        return this.name;
    }

    public int getDf(){
        return this.df;
    }

    public int getTf(){
        return this.tf;
    }
}
