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
    public void loadData(String dataPath) throws Exception{
        //Check correct JSON file validation
        JSONParser parser = new JSONParser();
        JSONArray jsonData = (JSONArray) parser.parse(new FileReader(dataPath));
        this.data = jsonData;
    }
    public void loadSchema(String schemaPath) throws Exception{
        JSONParser parser = new JSONParser();
        JSONObject jsObj = (JSONObject) parser.parse(new FileReader(schemaPath));
        JSONArray jsonSchema = (JSONArray) jsObj.get("schema");
        this.schema = jsonSchema;
    }
    public String show() throws Exception{
        String dataSet="";
        int dataSetLength = data.size();
        String keySet="", values="";
        boolean isHeaderSet = false;
        for(int i=0;i<dataSetLength;i++){
            JSONObject jsObj = (JSONObject) data.get(i);
            for(Object key:jsObj.keySet()){
                if(!isHeaderSet)
                   keySet += key+" , "; 
                values += jsObj.get(key)+" , ";      
            }
            isHeaderSet = true;
            values=values.substring(0, values.lastIndexOf(","))+"\n";
        }
        keySet=keySet.substring(0, keySet.lastIndexOf(","))+"\n";
        dataSet=keySet+values;
        return dataSet;
    }
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
    public List GroupBy(String col){
        List<String> measures = new ArrayList();
        HashMap<String, Vector<Vector>> measureDataSet = new HashMap();
        boolean isValidDimensionColumn = false;
        Vector columnNames = new Vector();
        columnNames.add(col);
        try{
            JSONArray jsArrSchema = schema;
            for(int i=0;i<jsArrSchema.size();i++){
                JSONObject element = (JSONObject) jsArrSchema.get(i);
                if(element.get("type").equals("dimension") && element.get("name").equals(col)){
                    isValidDimensionColumn = true;
                }
                if(element.get("type").equals("measure")){
                    measures.add(String.valueOf(element.get("name")));
                    columnNames.add(String.valueOf(element.get("name")));
                }
            } //Create a list of measure columns 
            
            JSONArray jsDataSet = data;
            int jsDataSetLength = jsDataSet.size();           
            for(int i=0;i<jsDataSetLength;i++){
                JSONObject groupListObj = (JSONObject) jsDataSet.get(i);
                Vector measureDataList = new Vector();
                Vector measuresData = new Vector();
                for(Object key:measures){
                    measuresData.add(groupListObj.get(key));    
                }
                if(measureDataSet.get(String.valueOf(groupListObj.get(col))) == null){
                    measureDataList.add(measuresData);
                    measureDataSet.put(String.valueOf(groupListObj.get(col)), measureDataList);
                }
                else{
                    measureDataList = measureDataSet.get(String.valueOf(groupListObj.get(col)));
                    measureDataList.add(measuresData);
                    measureDataSet.put(String.valueOf(groupListObj.get(col)), measureDataList);
                }
            }
            return avg(columnNames, measureDataSet, measures.size());            
        }catch(Exception ex){System.err.print(ex); return null;} 
    }

    public static List avg(Vector columnNames, HashMap<String, Vector<Vector>> data, int measureSize){
        List<String> result = new ArrayList();
        Pattern patternInt = Pattern.compile("[+-]?[0-9]+");
        //Pattern patternDouble = Pattern.compile("[+-]?[0-9]*\\.?[0-9]*");
        result.add(columnNames+"\n");
        for(String key:data.keySet()){
            Vector v = data.get(key);
            Vector rows = new Vector();
            rows.add(key);
            for(int j=0;j<measureSize;j++){
                double sum=0;
                 double valueDouble = 0.0;  
                 for(int i=0;i<v.size();i++){
                     Vector v1 = (Vector) v.get(i);
                     if(!String.valueOf(v1.get(j)).equals("null") || v1.get(j)!=null)
                         valueDouble = Double.parseDouble(v1.get(j).toString());
                     sum += valueDouble;
                 }
                 rows.add(sum);
            }
            result.add(rows+"\n");
        }
        return result;
    }

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
