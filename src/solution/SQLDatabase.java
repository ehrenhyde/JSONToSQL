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

		ArrayList<SQLValColumn> colsForMaster = this.createColsFromJSONObj(jsonObj);
		TreeMap<String, JSONSingleVal> rowForMaster = this.buildRowValues(jsonObj);

		SQLTable masterTable = new SQLTable(dbName,colsForMaster);
		masterTable.addRow(rowForMaster);
		this.addTable(dbName, masterTable);
	}

	private ArrayList<SQLValColumn> createColsFromJSONObj(JSONObj jsonObj) throws SQLObjException, JSONException {
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
			SQLTable forTable = this.createChildTable(objProp.getKey(), objProp.getValue());
			String forTableName = forTable.getName();
			String forColName = forTable.idColumnName();
			SQLColumnReference forColReference = new SQLColumnReference(forTableName, forColName);
			SQLForeignKeyColumn forKeyCol = new SQLForeignKeyColumn(colName, forColReference);
			cols.add(forKeyCol);
		}

		return cols;
	}

	private TreeMap<String, JSONSingleVal> buildRowValues(JSONObj jsonObj) throws JSONException, SQLObjException {

		TreeMap<String, JSONSingleVal> newRow = new TreeMap<String, JSONSingleVal>();

		// simpleValColumns
		TreeMap<String, JSONSingleVal> valProps = jsonObj.getValProps();

		for (Entry<String, JSONSingleVal> entry : valProps.entrySet()) {
			String columnName = entry.getKey();
			JSONSingleVal val = entry.getValue();
			newRow.put(columnName, val);
		}

		// foreignKeyColumns
		TreeMap<String, JSONObj> objProps = jsonObj.getObjProps();
		// the keys will be the columns, but the values will be idVals for other
		// columns in other tables

		for (Entry<String, JSONObj> objProp : objProps.entrySet()) {

			String colName = objProp.getKey();

			Integer forId = this.getChildTableReference(colName, objProp.getValue());
			JSONSingleVal colJSONVal = new JSONSingleVal("\"" + forId + "\"");
			newRow.put(colName, colJSONVal);
		}
		return newRow;
	}

	private Integer getChildTableReference(String childTableName, JSONObj jsonObj)
			throws SQLObjException, JSONException {
		SQLTable childTable;
		if (!this.childTableExists(childTableName)) {
			childTable = this.createChildTable(childTableName, jsonObj);
		} else {
			childTable = this.getTable(childTableName);
		}
		// write jsonObj into child table
		TreeMap<String, JSONSingleVal> row = this.buildRowValues(jsonObj);
		Integer idOfNewRow = childTable.addRow(row);

		return idOfNewRow;

	}

	private SQLTable createChildTable(String childTableName, JSONObj jsonObj) throws SQLObjException, JSONException {
		ArrayList<SQLValColumn> cols = this.createColsFromJSONObj(jsonObj);

		//TreeMap<String, JSONSingleVal> newRow = this.buildRowValues(jsonObj);

		SQLTable childTable = new SQLTable(childTableName,cols);
		//childTable.addRow(newRow);
		this.addTable(childTableName, childTable);
		return this.getTable(childTableName);
	}

	private boolean childTableExists(String childTableName) {
		return this.tables.containsKey(childTableName);
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
		/*
		 * return DriverManager.getConnection("jdbc:mysql://localhost/"+dbName+
		 * "?useSSL=false" + "&user=jsonToSQL&password="+password);
		 */
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
