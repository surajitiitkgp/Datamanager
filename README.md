# DataManager

DataManager is a library. It takes data and schema as input and performs various operations via data operators.

### Platform and environment details.

DataManager is built on JDK1.8. and Netbeans IDE 8.2


### Detailed installation procedure.

DataManager is very easy to install. Download the DataManager source code from the github repository and load this to NetBeans 8.2 or above version. Once the Project is loaded to the NetBeans IDE, make sure all the dependency library (org.json.simple) is inlcuded. 

### Build Command

```sh
NetNeans IDE > Open Project > Select the Project Directory (DataManager) > Open the project > Clean and Build
```

After the building the application the ``DataManager.jar`` file is created, which is located in ``dist`` folder. This jar file can be imported to any java class to perform the various operations via data operators.

#### Test Command

Create a Java class file (Test.java). Import the DataManager as shown below.

```sh
1. import com.chartsHQ.DataManager; 
2. DataManager dm = new DataManager();
3. dm.loadData('../path-of-data');
4. dm.loadSchema('../path-of-schema')
5. dm.show();
6. dm.project(columns[]);
7. dm.groupBy(column)
8. dm.select(column, <condition>)
```

### Usage of Data Operators

* ``loadData('data path')`` This function takes the JSON file as an input and load the data ([cars.json](https://raw.githubusercontent.com/surajitiitkgp/Datamanager/master/cars.json)). Exmaple: ``dm.loadData('cars.json')``.
* ``loadSchema('schema path')`` This function takes the JSON schema as an input and load the schema ([schema.json](https://raw.githubusercontent.com/surajitiitkgp/Datamanager/master/schema.json)). Exmaple: ``dm.loadSchema('schema.json')``.
* ``show()`` This function is called to display the entire dataset. Exmaple: ``dm.show()``.
* ``project('list of column names')`` This function is called to print only selected data object. Exmaple: 
```sh
String columns[] = {"Name", "Maker", "Origin", "Year"};
dm.project(columns);
```
* ``groupBy('Dimension column')`` This function takes a column name as an input. The name of a column must be a 'dimension'. It returns all measure fields with corresponding aggregated ( mean ) values and given 'column' field. Example: ``dm.groupBy("Maker")``.
* ``select(column, <condition>)`` This function takes accept two parameter as an input, column name and conditions. Then returns the only values which datisfies the condition.
