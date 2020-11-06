/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chartsHQ;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author SURAJIT
 */
public class DataManager {
    JSONArray data = new JSONArray();
    JSONArray schema = new JSONArray();
    
    /**
     * Load the data object of the given JSON path.
     *
     * @param dataPath
     *              The dataPath must be a valid JSON data source path.
     * @throws Exception
     *             If there is an invalid JSON or unable to parse the JSON.
     */ 
    public void loadData(String dataPath) throws Exception{ 
        JSONParser parser = new JSONParser(); 
        JSONArray jsonData = (JSONArray) parser.parse(new FileReader(dataPath));// Parses the json data
        this.data = jsonData;   //Set the JSON data in global variable
    }
    
    /**
     * Load the schema object of the given JSON path.
     *
     * @param schemaPath
     *              The schemaPath must be a valid JSON schema file.
     * @throws Exception
     *             If there is an invalid JSON or unable to parse the JSON.
     */     
    public void loadSchema(String schemaPath) throws Exception{
        JSONParser parser = new JSONParser();
        JSONObject jsObj = (JSONObject) parser.parse(new FileReader(schemaPath));// Parses the json data
        JSONArray jsonSchema = (JSONArray) jsObj.get("schema");
        this.schema = jsonSchema; //Set the schema information in global variable
    }
    
    /**
     * View the dataset .
     *
     * 
     * @return The entire dataset.
     *
     */
    public String show(){
        String dataSet="";
        int dataSetLength = data.size();
        String keySet="", values="";
        boolean isHeaderSet = false;    //Weather column namnes are set on the top of the data
        for(int i=0;i<dataSetLength;i++){  //Loop for reading each JSON Object  
            JSONObject jsObj = (JSONObject) data.get(i); //Parsing each data object from JSONArray
            for(Object key:jsObj.keySet()){    //Loop for reading value of the corresponding key
                if(!isHeaderSet)
                   keySet += key+" , ";     //Get the Column names
                values += jsObj.get(key)+" , ";    //Get the value of all dimensions and measures  
            }
            isHeaderSet = true;
            values=values.substring(0, values.lastIndexOf(","))+"\n";   //Remove the last "," and add line break;
        }
        keySet=keySet.substring(0, keySet.lastIndexOf(","))+"\n";   //Remove the last "," and add line break;
        dataSet=keySet+values;
        return dataSet;
    }
    /**
     * Project the dataset on given column name.
     *
     * @param col[]
     *             col[] is the list of columns, which want to project.
     * 
     * @return The corresponding the of the column.
     *
     */    
    public String project(String col[]) throws Exception{
        String dataSet="";
        int dataSetLength = data.size();
        String keySet="", values="";
        for(int i=0;i<dataSetLength;i++){
            JSONObject jsObj = (JSONObject) data.get(i);
            for(Object key:col){
                    if(i==0)
                       keySet += key+" , "; 
                    values += jsObj.get(key)+" , ";      
            }
            values=values.substring(0, values.lastIndexOf(","))+"\n";
        }
        keySet=keySet.substring(0, keySet.lastIndexOf(","))+"\n";
        dataSet=keySet+values;
        return dataSet;
    }
    /**
     * Group the dataset on given dimension.
     *
     * @param col
     *             col is the dimension name.
     * 
     * @return The aggregated ( mean ) according the dimension.
     *
     */     
    public List GroupBy(String col){
        List<String> measures = new ArrayList();
        HashMap<String, Vector<Vector>> measureDataSet = new HashMap();
        boolean isValidDimensionColumn = false;
        Vector columnNames = new Vector();
        columnNames.add(col);
        try{
            JSONArray jsArrSchema = schema;
            for(int i=0;i<jsArrSchema.size();i++){  //Create a list of measure columns
                JSONObject element = (JSONObject) jsArrSchema.get(i);
                if(element.get("type").equals("dimension") && element.get("name").equals(col)){
                    isValidDimensionColumn = true;
                }
                if(element.get("type").equals("measure")){
                    measures.add(String.valueOf(element.get("name")));
                    columnNames.add(String.valueOf(element.get("name")));
                }
            }  
            
            JSONArray jsDataSet = data;
            int jsDataSetLength = jsDataSet.size();           
            for(int i=0;i<jsDataSetLength;i++){
                JSONObject groupListObj = (JSONObject) jsDataSet.get(i);
                Vector measureDataList = new Vector();
                Vector measuresData = new Vector();
                for(Object key:measures){
                    measuresData.add(groupListObj.get(key));  //add the value of measures in vector  
                }
                if(measureDataSet.get(String.valueOf(groupListObj.get(col))) == null){ //If the hasmap values is empty of the corresponding key
                    measureDataList.add(measuresData);
                    measureDataSet.put(String.valueOf(groupListObj.get(col)), measureDataList);
                }
                else{   //If there is already some value in has table, get these and add the new.
                    measureDataList = measureDataSet.get(String.valueOf(groupListObj.get(col)));
                    measureDataList.add(measuresData);
                    measureDataSet.put(String.valueOf(groupListObj.get(col)), measureDataList);
                }
            }
            return avg(columnNames, measureDataSet, measures.size());   //return the agregated mean         
        }catch(Exception ex){System.err.print(ex); return null;} 
    }

    
    /**
     * Get the mean of the group of measure
     *
     * @param columnNames, data, easureSize
     *             columnNames is the list of column needs to be projected.
     *             data is the Hash map of the group of object.
     *             measureSize is the number of measure column.
     * 
     * @return The aggregated ( mean ) according the dimension.
     *
     */     
    public List avg(Vector columnNames, HashMap<String, Vector<Vector>> data, int measureSize){
        List<String> result = new ArrayList();
        result.add(columnNames+"\n");
        for(String key:data.keySet()){
            Vector v = data.get(key);
            Vector rows = new Vector();
            rows.add(key);
            for(int j=0;j<measureSize;j++){ //Sum elements at same index of the vectors into a single vector
                double sum=0; 
                 for(int i=0;i<v.size();i++){
                     double valueDouble = 0.0;
                     Vector v1 = (Vector) v.get(i);
                     if(!String.valueOf(v1.get(j)).equals("null") || v1.get(j)!=null) //If null, it will be 0.0 (default)
                         valueDouble = Double.parseDouble(v1.get(j).toString());
                     sum += valueDouble;
                 }
                 rows.add(sum/v.size()); //Calculating mean
            }
            result.add(rows+"\n");
        }
        return result;
    }
    
    /**
     * Project the dataset based on following condition
     *
     * @param col, predicate
     *             col is the name of the column on which we want to add filter.
     *             predicate is the condition.
     * 
     * @return List of filtered dataset.
     *
     */ 
    public List select(String col, Predicate<Double> predicate){
        List<String> result = new ArrayList();
        try{
            JSONArray jsDataSet = data;
            int jsDataSetLength = jsDataSet.size();

            boolean isHeaderSet = false;
            Vector keySet = new Vector();
            for(int i=0;i<jsDataSetLength;i++){
                //String keySet="", values="";
                Vector values = new Vector();
                JSONObject jsObj = (JSONObject) jsDataSet.get(i);
                if(predicate.test(Double.valueOf(jsObj.get(col).toString()))){ //Evaluates the condition
                for(Object key:jsObj.keySet()){
                        if(!isHeaderSet)
                           keySet.add(key); 
                            values.add(jsObj.get(key));      
                }
                if(!isHeaderSet){
                    result.add(keySet+"\n");
                }
                result.add(values+"\n");
                }
                isHeaderSet = true;
            }
                    
            
        }catch(Exception ex){} 
        return result;
    }    
    
    
    
}
