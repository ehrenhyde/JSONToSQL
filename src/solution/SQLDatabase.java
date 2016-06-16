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
	private final String postfixForElements = "_e";
	public String dbName;
	TreeMap<String, SQLTable> tables;
	private ArrayList<String> writtenTables;

	private Connection con;
	private int uniqueNumber;
	private TreeMap<SQLColumnLink,String> childTableNames;

	private void commonConstructor(String dbName) throws SQLException, IOException {
		this.dbName = dbName;
		this.tables = new TreeMap<String, SQLTable>();
		this.writtenTables = new ArrayList<String>();
		this.childTableNames = new TreeMap<SQLColumnLink,String>();
		this.uniqueNumber = 0;

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
		
		SQLColumnReference refToMasterId = masterTable.idColRef();
		ArrayList<SQLValColumn> colsForMaster = this.createColsFromJSONObj(refToMasterId, jsonObj);
		masterTable.addValColumns(colsForMaster);
		Integer idOfNextRow = masterTable.nextRowId();
	
		TreeMap<String, JSONSingleVal> rowForMaster = this.buildSingleRowValues(jsonObj,refToMasterId,idOfNextRow);
		masterTable.addRow(rowForMaster);
		
		this.addTable(dbName, masterTable);
	}

	public SQLDatabase(String dbName, JSONArray jsonArray) throws SQLException, IOException, JSONException, SQLObjException {
		this.commonConstructor(dbName);
		SQLTable masterTable = new SQLTable(dbName);
		
		SQLColumnReference refToMasterId = masterTable.idColRef();
		ArrayList<SQLValColumn> colsForMaster = this.createColsFromJSONArray(refToMasterId, jsonArray);
		masterTable.addValColumns(colsForMaster);
		
		this.populateMasterTableValues(masterTable,jsonArray,refToMasterId);
		
		this.addTable(dbName, masterTable);
	}

	public SQLDatabase(String dbName, JSONSingleVal jsonSingleVal) throws JSONException, SQLObjException, SQLException, IOException {
		this.commonConstructor(dbName);
		SQLTable masterTable = new SQLTable(dbName);
		
		ArrayList<SQLValColumn> colsForMaster = new ArrayList<SQLValColumn>();
		SQLValColumn valCol = new SQLSimpleValColumn("value");
		colsForMaster.add(valCol);
		masterTable.addValColumns(colsForMaster);
		
		TreeMap<String, JSONSingleVal> rowForMaster = this.buildSingleRowValues(jsonSingleVal);
		masterTable.addRow(rowForMaster);
		
		this.addTable(dbName, masterTable);
	}

	private void populateMasterTableValues(SQLTable masterTable, JSONArray jsonArray,SQLColumnReference idColRef) throws SQLObjException, JSONException {
		
		JSONElementType subElementType = jsonArray.getSubElementType();
		if (subElementType == JSONElementType.SINGLE_VAL){
			for (JSONElement jsonElement: jsonArray){
				JSONSingleVal jsonSingleVal = (JSONSingleVal) jsonElement;
				TreeMap<String, JSONSingleVal> row = this.buildSingleRowValues(jsonSingleVal);
				masterTable.addRow(row);
			}
		}else if (subElementType == JSONElementType.OBJECT){
			for (JSONElement jsonElement : jsonArray){
				JSONObj jsonObj = (JSONObj) jsonElement;
				Integer rowId = masterTable.nextRowId();
				TreeMap<String, JSONSingleVal> row = this.buildSingleRowValues(jsonObj, idColRef, rowId);
				masterTable.addRow(row);
			}
		}else if (subElementType == JSONElementType.ARRAY){
			for (JSONElement jsonElement : jsonArray){
				JSONArray jsonSubArray = (JSONArray) jsonElement;
				Integer rowId = masterTable.nextRowId();
				
				TreeMap<String, JSONSingleVal> row = this.buildSingleRowValuesForMaster(jsonSubArray, idColRef, this.postfixForElements, rowId);
				masterTable.addRow(row);
			}
		}else{
			throw new SQLObjException("unknown subElementType found when populating master table values");
		}
	}

	private ArrayList<SQLValColumn> createColsFromJSONObj(SQLColumnReference refToThisTableIdCol, JSONObj jsonObj) throws SQLObjException, JSONException {
		ArrayList<SQLValColumn> cols = new ArrayList<SQLValColumn>();
		// simpleValColumns
		TreeMap<String, JSONSingleVal> valProps = jsonObj.getValProps();

		for (Entry<String, JSONSingleVal> entry : valProps.entrySet()) {
			String columnName = entry.getKey().replaceAll("\\s", "");
			SQLValColumn simpleValCol = new SQLSimpleValColumn(columnName);
			cols.add(simpleValCol);
		}

		// foreignKeyColumns
		TreeMap<String, JSONObj> objProps = jsonObj.getObjProps();
		for (Entry<String, JSONObj> objProp : objProps.entrySet()) {

			String colName = objProp.getKey().replaceAll("\\s", "");
			SQLColumnReference refToCol = new SQLColumnReference(refToThisTableIdCol.getTableName(),colName);
			String childTableName = this.assureChildTableNameWithParentCol(refToCol);
			SQLTable forTable = this.assureTable(childTableName, objProp.getValue());
			SQLColumnReference forColReference = forTable.idColRef();
			SQLForeignKeyColumn forKeyCol = new SQLForeignKeyColumn(colName, forColReference);
			cols.add(forKeyCol);
		}
		
		//with arrayProps the child contains the foreignKey to the parent's id
		TreeMap<String, JSONArray> arrayProps = jsonObj.getArrayProps();
		for (Entry<String, JSONArray> arrayProp : arrayProps.entrySet()) {
			String propertyName = arrayProp.getKey().replaceAll("\\s", "");;
			JSONArray propertyValueArray = arrayProp.getValue();
			String childTableName = this.assureChildTableNameWithoutParentCol(refToThisTableIdCol,propertyName, propertyName,true);
			this.assureTable(childTableName, propertyValueArray,refToThisTableIdCol);
		}

		return cols;
	}

	private String assureChildTableNameWithParentCol(SQLColumnReference refToCol) throws SQLObjException {
		String childTableName;
		SQLColumnLink colLink = new SQLColumnLink(refToCol, refToCol.getColumnName());
		if (this.childNameKeyFound(colLink)){
			childTableName = this.retrieveChildTableName(colLink);
		}else{
			childTableName = this.generateChildTableNameWithParentCol(refToCol);
		}
		return childTableName;
	}

	private String assureChildTableNameWithoutParentCol(SQLColumnReference parentColRef, String parentAssociatedProp,
			String otherBit, boolean otherBitAloneOk) throws SQLObjException {
		SQLColumnLink colLink = new SQLColumnLink(parentColRef, parentAssociatedProp);
		String childTableName;
		if (this.childNameKeyFound(colLink)){
			childTableName = this.retrieveChildTableName(colLink);
		}else{
			childTableName = this.generateChildTableNameWithoutParentCol(parentColRef,parentAssociatedProp, otherBit,otherBitAloneOk);
		}
		return childTableName;
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
				TreeMap<String,JSONSingleVal> newRow = this.buildSingleRowValues( jsonObj,targetTable.idColRef() ,targetTable.nextRowId());
					String refToParentColName = parentIdColRef.toString();
					JSONSingleVal refToParentRow = new JSONSingleVal("\""+parentRowId+"\"");
					newRow.put(refToParentColName, refToParentRow);
				targetTable.addRow(newRow);
			}
		}else if (arrayElementType == JSONElementType.ARRAY){
			for (JSONElement jsonElement : jsonArray){
				JSONArray jsonSubArray = (JSONArray) jsonElement;
				TreeMap<String,JSONSingleVal> newRow = this.buildSingleRowValues(jsonSubArray,targetTable.idColRef(),this.postfixForElements, targetTable.nextRowId(),parentIdColRef,parentRowId);
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
	private TreeMap<String, JSONSingleVal> buildSingleRowValues(JSONArray jsonArray,SQLColumnReference idColRef,String nameOfProp, Integer rowId,SQLColumnReference parentIdColRef,Integer parentRowId) throws JSONException, SQLObjException {
		
		TreeMap<String,JSONSingleVal> newRow = new TreeMap<String,JSONSingleVal>();
		
		newRow.put(parentIdColRef.toString(), new JSONSingleVal("\""+parentRowId+"\""));
		
		SQLColumnLink linkFromChildToThis = new SQLColumnLink(idColRef,nameOfProp);
		
		String childTableName = this.retrieveChildTableName(linkFromChildToThis);
		
		SQLTable childTable = this.getTable(childTableName);
		this.populateTargetTableValues(childTable, jsonArray, idColRef, rowId);
		
		return newRow;

	}
	
	private TreeMap<String, JSONSingleVal> buildSingleRowValuesForMaster(JSONArray jsonArray,SQLColumnReference idColRef,String nameOfProp, Integer rowId) throws JSONException, SQLObjException {
TreeMap<String,JSONSingleVal> newRow = new TreeMap<String,JSONSingleVal>();
		
		SQLColumnLink linkFromChildToThis = new SQLColumnLink(idColRef,nameOfProp);
		
		String childTableName = this.retrieveChildTableName(linkFromChildToThis);
		
		SQLTable childTable = this.getTable(childTableName);
		this.populateTargetTableValues(childTable, jsonArray, idColRef, rowId);
		
		return newRow;
	}
	
	private String generateChildTableNameWithoutParentCol(SQLColumnReference parentIdColRef,String parentAssociatedProp,String otherBit,boolean otherBitAloneOk){
		String childTableName;
		String parentTableName = parentIdColRef.getTableName();
		
		if (otherBitAloneOk){
			childTableName = otherBit;
		}else{
			childTableName= parentTableName + otherBit;
		}
		
		if (this.childTableNames.containsValue(childTableName)){
			childTableName = parentTableName + otherBit;
		}
		
		if (this.childTableNames.containsValue(childTableName)){
			childTableName = childTableName + this.getNextUniqueNum();
		}
		
		SQLColumnLink key = new SQLColumnLink(parentIdColRef,parentAssociatedProp);
		
		this.childTableNames.put(key, childTableName);
		
		return childTableName;
	}
	
	private String generateChildTableNameWithParentCol(SQLColumnReference parentColRef){
		String childTableName;
		String parentColName = parentColRef.getColumnName();
		String parentTableName = parentColRef.getTableName();
		String parentAssociatedProp = parentColRef.getColumnName();
		
		childTableName = parentColName;
		
		if (this.childTableNames.containsValue(childTableName)){
			childTableName=parentTableName+childTableName;
		}
		if (this.childTableNames.containsValue(childTableName)){
			childTableName = childTableName + this.getNextUniqueNum();
		}
		
		SQLColumnLink key = new SQLColumnLink(parentColRef,parentAssociatedProp);
		
		this.childTableNames.put(key, childTableName);
		
		return childTableName;
	}
	
	private Integer getNextUniqueNum() {
		this.uniqueNumber++;
		return this.uniqueNumber;
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
			String columnName = entry.getKey().replaceAll("\\s", "");;
			JSONSingleVal val = entry.getValue();
			newRow.put(columnName, val);
		}

		// foreignKeyColumns
		//with the objProp we add the key to this table
		TreeMap<String, JSONObj> objProps = jsonObj.getObjProps();

		for (Entry<String, JSONObj> objProp : objProps.entrySet()) {

			String colName = objProp.getKey().replaceAll("\\s", "");;
			SQLColumnReference refToProp = new SQLColumnReference(idColRef.getTableName(),colName);
			SQLColumnLink linkToThisTablesProp = new SQLColumnLink(refToProp,colName);
			String childTableName = this.retrieveChildTableName(linkToThisTablesProp);
			
			Integer forId = this.getChildTableReferenceRowId(childTableName, objProp.getValue());
			
			JSONSingleVal colJSONVal = new JSONSingleVal("\"" + forId + "\"");
			newRow.put(colName, colJSONVal);
		}
		
		//but for the arrayProps we added the key to the child table
		//so we need to ensure that the child is populated
		TreeMap<String, JSONArray> arrayProps = jsonObj.getArrayProps();
		for (Entry<String, JSONArray> arrayProp : arrayProps.entrySet()) {
			String colName = arrayProp.getKey().replaceAll("\\s", "");
			SQLColumnLink parentColLink = new SQLColumnLink(idColRef,colName);
			String childTableName = this.retrieveChildTableName(parentColLink);
			SQLTable childTable = this.getTable(childTableName);
			this.populateTargetTableValues(childTable,arrayProp.getValue(),idColRef,rowId);
		}
		return newRow;
	}
	
	private boolean childNameKeyFound(SQLColumnLink colLink){
		for (Entry<SQLColumnLink, String> chldTableEntry : this.childTableNames.entrySet()){
			SQLColumnLink colLinkEntryKey = chldTableEntry.getKey();
			if (colLinkEntryKey.equalsColLink(colLink)){
				return true;			
			}
		}
		return false;
	}

	private String retrieveChildTableName(SQLColumnLink colLink) throws SQLObjException {
		for (Entry<SQLColumnLink, String> chldTableEntry : this.childTableNames.entrySet()){
			SQLColumnLink colLinkEntryKey = chldTableEntry.getKey();
			if (colLinkEntryKey.equalsColLink(colLink)){
				return chldTableEntry.getValue();				
			}
		}
		throw new SQLObjException("Could not find colLink " + colLink.toString() + " in the childTableNames entries");
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
			ArrayList<SQLValColumn> cols = this.createColsFromJSONArray(refToAssuredTableIdCol, rowValues);
			
			SQLForeignKeyColumn refToParentCol = new SQLForeignKeyColumn(parentIdRef.toString(),parentIdRef);
			cols.add(refToParentCol);			
			
			assuredTable.addValColumns(cols);
			this.addTable(tableName, assuredTable);
		}else{
			SQLTable assuredTable = this.getTable(tableName);
			ArrayList<SQLValColumn> currentCols = assuredTable.getValCols();
			
			SQLColumnReference refToAssuredTableIdCol = assuredTable.idColRef();
			ArrayList<SQLValColumn> potentiallyNewCols = this.createColsFromJSONArray(refToAssuredTableIdCol, rowValues);
			ArrayList<SQLValColumn> newCols = this.subtractColumnArrayLists(potentiallyNewCols,currentCols);
			assuredTable.addValColumns(newCols);
		}
		return this.getTable(tableName);
		
	}
	
	private ArrayList<SQLValColumn> subtractColumnArrayLists(ArrayList<SQLValColumn> cols,
			ArrayList<SQLValColumn> colsToSubtract) {
		ArrayList<SQLValColumn> resCols = new ArrayList<SQLValColumn>();
		for (SQLValColumn col : cols){
			boolean found = false;
			for (SQLValColumn colToSubtract : colsToSubtract){
				if (colToSubtract.equalsCol(col)){
					found = true;
				}
			}
			if (found == false){
				resCols.add(col);
			}
		}
		return resCols;
	}

	private ArrayList<SQLValColumn> mergeColumnArrayLists(ArrayList<SQLValColumn> list1, ArrayList<SQLValColumn> list2){
		ArrayList<SQLValColumn> together = new ArrayList<SQLValColumn>();
		together.addAll(list1);
		for (SQLValColumn list2Col : list2){
			boolean found = false;
			for (SQLValColumn list1Col : list1){
				if (list1Col.equalsCol(list2Col)){
					found = true;
				}
			}
			if (found == false){
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
	private ArrayList<SQLValColumn> createColsFromJSONArray(SQLColumnReference refToThisTableIdCol, JSONArray jsonArray) throws SQLObjException, JSONException {
		
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
			
			String childTableName = this.assureChildTableNameWithoutParentCol(refToThisTableIdCol,this.postfixForElements,this.postfixForElements,false);
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
		this.executeUpdate("DROP DATABASE IF EXISTS " + dbName);
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
	
	public String getPostfixForElements(){
		return this.postfixForElements;
	}
}
