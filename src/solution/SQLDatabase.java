package solution;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class SQLDatabase {

	private final String pathToPassword = "C:\\Users\\Ehren\\OneDrive\\Documents\\Personal Projects\\Java Projects\\JSONToSQL\\resources\\secret\\password.txt";
	public String dbName;
	TreeMap<String, SQLTable> tables;
	private ArrayList<String> writtenTables;

	private Connection con;

	private void commonConstructor(String dbName) throws SQLException, IOException {
		this.dbName = dbName;
		this.tables = new TreeMap<String, SQLTable>();
		this.writtenTables = new ArrayList<String>();

		this.con = this.getConnection();

		this.createDb();
		this.setDefaultDb();
	}

	public SQLDatabase(String dbName) throws SQLException, IOException {
		this.commonConstructor(dbName);

	}

	public SQLDatabase(String dbName, JSONObj jsonObj)
			throws SQLException, IOException, SQLObjException, JSONException {
		this.commonConstructor(dbName);
		SQLTable masterTable = new SQLTable(dbName);
		
		SQLColumnReference refToMasterId = new SQLColumnReference(masterTable.getName(),masterTable.idColumnName());
		ArrayList<SQLValColumn> colsForMaster = this.createColsFromJSONObj(refToMasterId, jsonObj);
		masterTable.addValColumns(colsForMaster);
		Integer idOfNextRow = masterTable.nextRowId();
	
		SQLColumnReference idColRef = new SQLColumnReference(masterTable.getName(), masterTable.idColumnName());
		TreeMap<String, JSONSingleVal> rowForMaster = this.buildSingleRowValues(jsonObj,idColRef,idOfNextRow);
		masterTable.addRow(rowForMaster);
		
		this.addTable(dbName, masterTable);
	}

	private ArrayList<SQLValColumn> createColsFromJSONObj(SQLColumnReference refToThisTableIdCol, JSONObj jsonObj) throws SQLObjException, JSONException {
		ArrayList<SQLValColumn> cols = new ArrayList<SQLValColumn>();
		// simpleValColumns
		TreeMap<String, JSONSingleVal> valProps = jsonObj.getValProps();

		for (Entry<String, JSONSingleVal> entry : valProps.entrySet()) {
			String columnName = entry.getKey();
			SQLValColumn simpleValCol = new SQLSimpleValColumn(columnName);
			cols.add(simpleValCol);
		}

		// foreignKeyColumns
		TreeMap<String, JSONObj> objProps = jsonObj.getObjProps();
		for (Entry<String, JSONObj> objProp : objProps.entrySet()) {

			String colName = objProp.getKey();
			SQLTable forTable = this.assureTable(objProp.getKey(), objProp.getValue());
			SQLColumnReference forColReference = forTable.idColRef();
			SQLForeignKeyColumn forKeyCol = new SQLForeignKeyColumn(colName, forColReference);
			cols.add(forKeyCol);
		}
		
		//with arrayProps the child contains the foreignKey to the parent's id
		TreeMap<String, JSONArray> arrayProps = jsonObj.getArrayProps();
		for (Entry<String, JSONArray> arrayProp : arrayProps.entrySet()) {
			String propertyName = arrayProp.getKey();
			JSONArray propertyValueArray = arrayProp.getValue();
			this.assureTable(propertyName, propertyValueArray,refToThisTableIdCol);
		}

		return cols;
	}

	private void populateTargetTableValues(SQLTable targetTable, JSONArray jsonArray,SQLColumnReference parentIdColRef,Integer parentRowId) throws JSONException, SQLObjException {
		
		JSONElementType arrayElementType = jsonArray.getSubElementType();
		if (arrayElementType == JSONElementType.SINGLE_VAL){
			for (JSONElement jsonElement : jsonArray){
				JSONSingleVal jsonSingleVal = (JSONSingleVal) jsonElement;
				TreeMap<String,JSONSingleVal> newRow = this.buildSingleRowValues( jsonSingleVal);
				String refToParentColName = parentIdColRef.toString();
				JSONSingleVal refToParentRow = new JSONSingleVal("\""+parentRowId+"\"");
				newRow.put(refToParentColName, refToParentRow);
				targetTable.addRow(newRow);
			}
		}else if (arrayElementType == JSONElementType.OBJECT){
			for (JSONElement jsonElement : jsonArray){
				JSONObj jsonObj = (JSONObj) jsonElement;
				TreeMap<String,JSONSingleVal> newRow = this.buildSingleRowValues( jsonObj,targetTable.idColRef(), targetTable.nextRowId());
				String refToParentColName = parentIdColRef.toString();
				JSONSingleVal refToParentRow = new JSONSingleVal("\""+parentRowId+"\"");
				newRow.put(refToParentColName, refToParentRow);
				targetTable.addRow(newRow);
			}
		}else if (arrayElementType == JSONElementType.ARRAY){
			for (JSONElement jsonElement : jsonArray){
				JSONArray jsonSubArray = (JSONArray) jsonElement;
				TreeMap<String,JSONSingleVal> newRow = this.buildSingleRowValues(jsonSubArray,targetTable.idColRef(), targetTable.nextRowId(),parentIdColRef,parentRowId);
				targetTable.addRow(newRow);
			}
		}else{
			throw new SQLObjException("Unexpected array element type when populating table values from array");
		}
	}

	/**
	 * provides values for all fields excluding the id field
	 * and references to the parent
	 * @param jsonSingleVal
	 * @param idColRef
	 * @param rowId
	 * @return
	 * @throws JSONException
	 */
	private TreeMap<String, JSONSingleVal> buildSingleRowValues(JSONSingleVal jsonSingleVal) throws JSONException {
		
		TreeMap<String,JSONSingleVal> newRow = new TreeMap<String,JSONSingleVal>();	
		newRow.put("value", jsonSingleVal);
	
		return newRow;
		
	}

	/**
	 * provides values for all fields excluding the id field
	 * @param jsonArray
	 * @param idColRef
	 * @param rowId
	 * @return
	 * @throws JSONException 
	 * @throws SQLObjException 
	 */
	private TreeMap<String, JSONSingleVal> buildSingleRowValues(JSONArray jsonArray,SQLColumnReference idColRef, Integer rowId,SQLColumnReference parentIdColRef,Integer parentRowId) throws JSONException, SQLObjException {
		
		TreeMap<String,JSONSingleVal> newRow = new TreeMap<String,JSONSingleVal>();
		
		newRow.put(parentIdColRef.toString(), new JSONSingleVal("\""+parentRowId+"\""));
		
		String childTableName = idColRef.getTableName()+"_elements";
		
		SQLTable childTable = this.getTable(childTableName);
		this.populateTargetTableValues(childTable, jsonArray, idColRef, rowId);
		
		return newRow;

	}
	
	/**
	 * provides values for all fields excluding the id field and references back to a parent table
	 * @param jsonObj
	 * @param idColRef
	 * @param rowId
	 * @return
	 * @throws JSONException
	 * @throws SQLObjException
	 */
	private TreeMap<String, JSONSingleVal> buildSingleRowValues(JSONObj jsonObj,SQLColumnReference idColRef,Integer rowId) throws JSONException, SQLObjException {

		TreeMap<String, JSONSingleVal> newRow = new TreeMap<String, JSONSingleVal>();

		// simpleValColumns
		TreeMap<String, JSONSingleVal> valProps = jsonObj.getValProps();

		for (Entry<String, JSONSingleVal> entry : valProps.entrySet()) {
			String columnName = entry.getKey();
			JSONSingleVal val = entry.getValue();
			newRow.put(columnName, val);
		}

		// foreignKeyColumns
		//with the objProp we add the key to this table
		TreeMap<String, JSONObj> objProps = jsonObj.getObjProps();

		for (Entry<String, JSONObj> objProp : objProps.entrySet()) {

			String colName = objProp.getKey();

			Integer forId = this.getChildTableReferenceRowId(colName, objProp.getValue());
			JSONSingleVal colJSONVal = new JSONSingleVal("\"" + forId + "\"");
			newRow.put(colName, colJSONVal);
		}
		
		//but for the arrayProps we added the key to the child table
		//so we need to ensure that the child is populated
		TreeMap<String, JSONArray> arrayProps = jsonObj.getArrayProps();
		for (Entry<String, JSONArray> arrayProp : arrayProps.entrySet()) {

			String colName = arrayProp.getKey();

			SQLTable childTable = this.getTable(colName);
			this.populateTargetTableValues(childTable,arrayProp.getValue(),idColRef,rowId);
		}
		return newRow;
	}

	/**
	 * Adds a row to the child table and returns the id
	 */
	private Integer getChildTableReferenceRowId(String childTableName, JSONObj jsonObj)
			throws SQLObjException, JSONException {
		
		SQLTable childTable = this.assureTable(childTableName, jsonObj);
		Integer idOfNextRow = childTable.nextRowId();
		
		SQLColumnReference idColRef  = childTable.idColRef();
		TreeMap<String, JSONSingleVal> row = this.buildSingleRowValues(jsonObj,idColRef,idOfNextRow);
		Integer idOfNewRow = childTable.addRow(row);

		return idOfNewRow;

	}

	private SQLTable assureTable(String childTableName, JSONObj jsonObj) throws SQLObjException, JSONException {
		if(!this.tableExists(childTableName)){
			
			SQLTable childTable = new SQLTable(childTableName);
			
			SQLColumnReference refToThisTableIdCol = childTable.idColRef();
			ArrayList<SQLValColumn> cols = this.createColsFromJSONObj(refToThisTableIdCol, jsonObj);
			
			childTable.addValColumns(cols);
			
			this.addTable(childTableName, childTable);
		}
		
		return this.getTable(childTableName);
	}
	
	private SQLTable assureTable(String tableName, JSONArray rowValues,SQLColumnReference parentIdRef) throws SQLObjException, JSONException {
		if (!this.tableExists(tableName)){
			
			SQLTable assuredTable = new SQLTable(tableName);
			
			SQLColumnReference refToAssuredTableIdCol = assuredTable.idColRef();
			ArrayList<SQLValColumn> cols = this.createColsFromJSONArray(refToAssuredTableIdCol, rowValues, parentIdRef);
			
			SQLForeignKeyColumn refToParentCol = new SQLForeignKeyColumn(parentIdRef.toString(),parentIdRef);
			cols.add(refToParentCol);			
			
			assuredTable.addValColumns(cols);
			this.addTable(tableName, assuredTable);
		}
		return this.getTable(tableName);
		
	}
	
	private ArrayList<SQLValColumn> mergeColumnArrayLists(ArrayList<SQLValColumn> list1, ArrayList<SQLValColumn> list2){
		ArrayList<SQLValColumn> together = new ArrayList<SQLValColumn>();
		together.addAll(list2);
		for (SQLValColumn list2Col : list2){
			if (together.contains(list2Col)){
				together.add(list2Col);
			}
		}
		return together;
	}

	/**
	 * creates value holding columns based on the values of a JSONArray.
	 * Does not create the id column for a row
	 * Does not create the reference to parent when passed 2d arrays
	 * Can create child tables
	 * @param refToThisTableIdCol
	 * @param jsonArray
	 * @param parentIdRef
	 * @return
	 * @throws SQLObjException
	 * @throws JSONException
	 */
	private ArrayList<SQLValColumn> createColsFromJSONArray(SQLColumnReference refToThisTableIdCol, JSONArray jsonArray,
			SQLColumnReference parentIdRef) throws SQLObjException, JSONException {
		
		ArrayList<SQLValColumn> allCols = new ArrayList<SQLValColumn>();
		
		JSONElementType arrayElementType = jsonArray.getSubElementType();
		if (arrayElementType == JSONElementType.SINGLE_VAL){
			
			SQLValColumn valCol = new SQLSimpleValColumn("value");
			allCols.add(valCol);
			
		}else if (arrayElementType == JSONElementType.OBJECT){
			
			for(JSONElement jsonElement : jsonArray){
				JSONObj jsonObj = (JSONObj) jsonElement;
				ArrayList<SQLValColumn> assuredCols = this.createColsFromJSONObj(refToThisTableIdCol, jsonObj);
				allCols = this.mergeColumnArrayLists(allCols, assuredCols);
			}
			
		}else if (arrayElementType == JSONElementType.ARRAY){
			
			String childTableName = refToThisTableIdCol.getTableName()+"_elements";
			for (JSONElement jsonElement : jsonArray){
				JSONArray jsonSubArray = (JSONArray) jsonElement;
				this.assureTable(childTableName, jsonSubArray, refToThisTableIdCol);
			}
			
		}else{
			throw new SQLObjException("Trying to create list of cols from array containing JSONElements other than JSONObj or JSONArray or JSONSingleVal");
		}
		return allCols;
	}

	private boolean tableExists(String tableName) {
		return this.tables.containsKey(tableName);
	}

	private void createDb() throws SQLException {
		this.executeUpdate("create database " + dbName);
	}

	private void setDefaultDb() throws SQLException {
		this.executeUpdate("use " + dbName + ";");
	}

	public void terminate() throws SQLException {

		this.executeUpdate("use sys;");
		this.executeUpdate("drop database " + dbName + ";");

		for (Entry<String, SQLTable> table : this.tables.entrySet()) {
			table.getValue().setSchemaWritten(false);
		}

	}

	public void addTable(String tableName, SQLTable table) {
		this.tables.put(tableName, table);
	}

	private String getPassword() throws IOException {
		Path path = Paths.get(this.pathToPassword);
		String password = Files.readAllLines(path, StandardCharsets.UTF_8).get(0);
		return password;
	}

	private Connection getConnection() throws SQLException, IOException {
		String password = this.getPassword();
		return DriverManager
				.getConnection("jdbc:mysql://localhost/?useSSL=false" + "&user=jsonToSQL&password=" + password);

	}

	private SQLTable getTable(String tableName) throws SQLObjException {
		if (this.tables.containsKey(tableName)) {
			return this.tables.get(tableName);
		} else {
			throw new SQLObjException("Trying to get table which doesn't exist in this database");
		}
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		// Statements allow to issue SQL queries to the database
		Statement stmt = con.createStatement();
		// Result set get the result of the SQL query
		ResultSet resultSet = stmt.executeQuery(sql);
		return resultSet;
	}

	public void executeUpdate(String sql) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate(sql);
	}

	public void writeTableSchema(String tableName) throws SQLException, SQLObjException {
		SQLTable table = this.tables.get(tableName);
		String tableSQL = table.tableSQL(dbName, tableName);
		this.executeUpdate(tableSQL);
		table.setSchemaWritten(true);
	}

	public void writeTableValues(String tableName) throws SQLObjException, SQLException {
		SQLTable table = this.getTable(tableName);
		String valuesSQL = table.valuesInsertSQL(dbName, tableName);
		this.executeUpdate(valuesSQL);

		this.writtenTables.add(tableName);
	}

	public void writeTable(String tableName) throws SQLException, SQLObjException {
		this.writeTableSchema(tableName);
		this.writeTableValues(tableName);
	}

	public void writeAll() throws SQLException, SQLObjException {

		while (!this.allTablesWritten()) {
			for (Entry<String, SQLTable> tableEntry : this.tables.entrySet()) {
				SQLTable table = tableEntry.getValue();

				boolean dependenciesMet = this.allDependenciesMet(table);
				boolean alreadyWritten = this.tableWritten(table);
				if (dependenciesMet && !alreadyWritten) {
					this.writeTable(tableEntry.getKey());
				}
			}
		}
	}

	private boolean tableWritten(SQLTable table) {
		String tableName = table.getName();
		return this.writtenTables.contains(tableName);
	}

	private boolean allDependenciesMet(SQLTable table) {
		ArrayList<String> tableDependencies = table.getDependencyTableNames();
		return this.writtenTables.containsAll(tableDependencies);
	}

	private boolean allTablesWritten() {
		Set<String> allTableNames = this.tables.keySet();
		return this.writtenTables.containsAll(allTableNames);
	}
}
