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
        // Parses the json data
        JSONArray jsonData = (JSONArray) parser.parse(new FileReader(dataPath));
        //Set the JSON data in global variable
        this.data = jsonData;   
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
        // Parses the json data
        JSONObject jsObj = (JSONObject) parser.parse(new FileReader(schemaPath));
        JSONArray jsonSchema = (JSONArray) jsObj.get("schema");
        //Set the schema information in global variable
        this.schema = jsonSchema; 
    }
    
    /**
     * View the dataset .
     *
     * 
     * @return The entire dataset.
     *
     */
    public String show(){
        String dataSet = "";
        int dataSetLength = data.size();
        String keySet = "", values = "";
        //Weather column namnes are set on the top of the data
        boolean isHeaderSet = false;  
        //Loop for reading each JSON Object
        for(int i=0;i<dataSetLength;i++){    
            //Parsing each data object from JSONArray
            JSONObject jsObj = (JSONObject) data.get(i); 
            //Loop for reading value of the corresponding key
            for(Object key:jsObj.keySet()){    
                if(!isHeaderSet)
                   //Get the Column names
                   keySet += key+" , ";     
                //Get the value of all dimensions and measures
                values += jsObj.get(key)+" , ";      
            }
            isHeaderSet = true;
            //Remove the last "," and add line break;
            values = values.substring(0, values.lastIndexOf(","))+"\n";   
        }
        //Remove the last "," and add line break;
        keySet = keySet.substring(0, keySet.lastIndexOf(","))+"\n";   
        dataSet = keySet+values;
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
        String dataSet = "";
        int dataSetLength = data.size();
        String keySet = "", values = "";
        for(int i = 0 ; i<dataSetLength ; i++){
            JSONObject jsObj = (JSONObject) data.get(i);
            for(Object key:col){
                    if(i == 0)
                       keySet += key+" , "; 
                    values += jsObj.get(key)+" , ";      
            }
            values = values.substring(0, values.lastIndexOf(","))+"\n";
        }
        keySet = keySet.substring(0, keySet.lastIndexOf(","))+"\n";
        dataSet = keySet+values;
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
            //Create a list of measure columns
            for(int i = 0 ; i<jsArrSchema.size() ; i++){  
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
            for(int i = 0 ; i<jsDataSetLength ; i++){
                JSONObject groupListObj = (JSONObject) jsDataSet.get(i);
                Vector measureDataList = new Vector();
                Vector measuresData = new Vector();
                for(Object key:measures){
                    //add the value of measures in vector
                    measuresData.add(groupListObj.get(key));    
                }
                //If the hasmap values is empty of the corresponding key
                if(measureDataSet.get(String.valueOf(groupListObj.get(col))) == null){ 
                    measureDataList.add(measuresData);
                    measureDataSet.put(String.valueOf(groupListObj.get(col)), measureDataList);
                }
                //If there is already some value in has table, get these and add the new.
                else{   
                    measureDataList = measureDataSet.get(String.valueOf(groupListObj.get(col)));
                    measureDataList.add(measuresData);
                    measureDataSet.put(String.valueOf(groupListObj.get(col)), measureDataList);
                }
            }
            //return the agregated mean
            return avg(columnNames, measureDataSet, measures.size());            
        }catch(Exception ex){System.err.print(ex); return null;} 
    }

    
    /**
     * Get the mean of the group of measure
     *
     * @param columnNames
     *             columnNames is the list of column needs to be projected.
     * @param data
     *             data is the Hash map of the group of object.
     * @param  measureSize
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
            //Sum elements at same index of the vectors into a single vector
            for(int j = 0 ; j<measureSize ; j++){ 
                double sum = 0; 
                 for(int i = 0 ; i<v.size() ; i++){
                     double valueDouble = 0.0;
                     Vector v1 = (Vector) v.get(i);
                     if(!String.valueOf(v1.get(j)).equals("null") || v1.get(j) != null) //If null, it will be 0.0 (default)
                         valueDouble = Double.parseDouble(v1.get(j).toString());
                     sum += valueDouble;
                 }
                 //Calculating mean
                 rows.add(sum/v.size()); 
            }
            result.add(rows+"\n");
        }
        return result;
    }
    
    /**
     * Project the dataset based on following condition
     *
     * @param col
     *             col is the name of the column on which we want to add filter.
     * @param predicate
     *             predicate is the boolean-valued function.
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
            for(int i = 0 ; i<jsDataSetLength ; i++){
                //String keySet="", values="";
                Vector values = new Vector();
                JSONObject jsObj = (JSONObject) jsDataSet.get(i);
                //Evaluates the condition
                if(predicate.test(Double.valueOf(jsObj.get(col).toString()))){ 
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
