package solution;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

public class SQLTable {
	private SQLIdColumn idColumn;
	private TreeMap<String, SQLValColumn> columns;
	private boolean schemaWritten;
	private String tableName;
	
	private void setValCols(ArrayList<SQLValColumn> cols){
		TreeMap<String,SQLValColumn> colsMap = new TreeMap<String,SQLValColumn>();
		for (SQLValColumn col : cols){
			colsMap.put(col.getName(), col);
		}
		this.columns = colsMap;
	}
	private void commonConstructor(String name,ArrayList<SQLValColumn> cols){
		this.tableName = name;
		this.setValCols(cols);
		this.schemaWritten = false;
		this.idColumn = new SQLIdColumn();
	}

	public SQLTable(String name) {
		ArrayList<SQLValColumn> cols = new ArrayList<SQLValColumn>();
		this.commonConstructor(name,cols);
	}
	
	public void addValColumns(ArrayList<SQLValColumn> cols){
		this.setValCols(cols);
	}
	
	public SQLTable(String name,ArrayList<SQLValColumn> cols){
		this.commonConstructor(name,cols);
	}

	public Integer addRow(TreeMap<String, JSONSingleVal> vals) throws SQLObjException {
		Integer newId = this.idColumn.addKey();
		for (Entry<String, JSONSingleVal> keyValPair : vals.entrySet()) {
			String columnName = keyValPair.getKey();
			JSONSingleVal cellVal = keyValPair.getValue();

			if (!this.columns.containsKey(columnName)) {
				this.columns.put(columnName, new SQLSimpleValColumn(columnName));
			}
			
			SQLValColumn column = this.columns.get(columnName);
			column.addCell(newId, cellVal);
			this.columns.put(columnName, column);
		}
		return newId;
	}

	public void addForeignKeyColumn(String columnName, SQLColumnReference referenceColumnName) {
		SQLForeignKeyColumn column = new SQLForeignKeyColumn(columnName,referenceColumnName);
		this.columns.put(columnName, column);
	}

	public void addValueColumn(String columnName) {
		this.columns.put(columnName, new SQLSimpleValColumn(columnName));
	}

	public TreeMap<String, SQLSimpleValColumn> getSimpleValCols() {
		TreeMap<String, SQLSimpleValColumn> simpleValCols = new TreeMap<String, SQLSimpleValColumn>();
		for (Entry<String, SQLValColumn> nameAndCol : this.columns.entrySet()) {
			if (nameAndCol.getValue() instanceof SQLSimpleValColumn) {
				simpleValCols.put(nameAndCol.getKey(), (SQLSimpleValColumn) nameAndCol.getValue());
			}
		}
		return simpleValCols;
	}

	public TreeMap<String, SQLForeignKeyColumn> getForeignKeyCols() {
		TreeMap<String, SQLForeignKeyColumn> foreignKeyCols = new TreeMap<String, SQLForeignKeyColumn>();
		for (Entry<String, SQLValColumn> nameAndCol : this.columns.entrySet()) {
			if (nameAndCol.getValue() instanceof SQLForeignKeyColumn) {
				foreignKeyCols.put(nameAndCol.getKey(), (SQLForeignKeyColumn) nameAndCol.getValue());
			}
		}
		return foreignKeyCols;
	}

	private String sqlStatementHeader(String dbName, String tableName) {
		return "CREATE TABLE `" + dbName + "`." + "`" + tableName + "` (";
	}

	private String sqlStatementPrimaryKeyCol() {
		String keyColName = this.idColumn.getName();

		String sql = "";
		sql += "`" + keyColName + "` INTEGER NOT NULL,";
		sql += " PRIMARY KEY (`" + keyColName + "`)";
		return sql;
	}

	private String sqlStatementSimpleValCol(String colName, SQLSimpleValColumn col) {
		String sql = "";
		sql += "`" + colName + "` VARCHAR(1024) NULL";
		return sql;
	}

	private String sqlStatementForeignKeyCol(String dbName, String colName, SQLForeignKeyColumn col) {
		SQLColumnReference ref = col.getColumnReference();
		String foreignTableName = ref.getTableName();
		String foreignColumnName = ref.getColumnName();

		String constraintName = colName + "_" + foreignColumnName;
		String sql = "";
		sql += "`" + colName + "` INTEGER NOT NULL, ";
		sql += "INDEX `" + colName + "_idx` (`" + colName + "` ASC),";
		sql += "CONSTRAINT `" + constraintName + "` ";
		sql += "FOREIGN KEY (`" + colName + "`) ";
		sql += "REFERENCES `" + dbName + "`.`" + foreignTableName + "` (`" + foreignColumnName + "`) ";
		sql += "ON DELETE NO ACTION ";
		sql += "ON UPDATE NO ACTION";
		return sql;
	}

	public String tableSQL(String dbName, String tableName) {
		String sql = "";
		sql += this.sqlStatementHeader(dbName, tableName);
		sql += this.sqlStatementPrimaryKeyCol();
		for (Entry<String, SQLValColumn> keyVal : this.columns.entrySet()) {

			sql += ",";

			String columnName = keyVal.getKey();
			SQLValColumn col = keyVal.getValue();
			if (col instanceof SQLSimpleValColumn) {
				SQLSimpleValColumn simpleValCol = (SQLSimpleValColumn) col;
				sql += this.sqlStatementSimpleValCol(columnName, simpleValCol);
			} else if (col instanceof SQLForeignKeyColumn) {
				SQLForeignKeyColumn forCol = (SQLForeignKeyColumn) col;
				sql += this.sqlStatementForeignKeyCol(dbName, columnName, forCol);
			}

		}
		sql += ");";
		return sql;
	}
	
	private String sepValuesByComma(ArrayList<String> strings){
		String list = "";
		boolean first = true;
		for (String string : strings){
			if (!first){
				list+=",";
			}
			list+=string;
			first = false;
		}
		return list;
	}
	
	private ArrayList<String> otherColumnNames(){
		ArrayList<String> colNames = new ArrayList<String>();
		for (String colName : this.columns.keySet()){
			colNames.add(colName);
		}
		return colNames;
	}
	
	private TreeMap<String,String> valuesForId(Integer rowId){
		
		String idColName = this.idColumnName();
		
		TreeMap<String,String> row = new TreeMap<String,String>();
		
		row.put(idColName, rowId+"");
		
		for(Entry<String,SQLValColumn> colEntry : this.columns.entrySet()){
			String colName = colEntry.getKey();
			SQLValColumn col = colEntry.getValue();
			if (col.containsValForId(rowId)){
				row.put(colName, col.getValForId(rowId));
			}else{
				row.put(colName, null);
			}
		}
		return row;
	}
	
	public String idColumnName(){
		return this.idColumn.getName();
	}

	
	private String rowValuesString(Integer id,ArrayList<String> colsInOrder){
		String sql = "(";
		boolean first = true;
		TreeMap<String,String> row = this.valuesForId(id);
		for (String colName : colsInOrder){
			if (!first){
				sql+=",";
			}else{
				first = false;
			}
			String colVal = row.get(colName);
			if (colVal != null){
				sql+="'" + colVal + "'";
			}else{
				sql+="null";
			}
		}
		sql+=")";
		return sql;
	}
	
	private ArrayList<String> allColumnNames(){
		ArrayList<String> allColumnNames = new ArrayList<String>();
		allColumnNames.add(this.idColumnName());
		allColumnNames.addAll(this.otherColumnNames());
		return allColumnNames;
	}

	public String valuesInsertSQL(String dbName, String tableName) throws SQLObjException {
		String sql="";
		ArrayList<Integer> ids = idColumn.getKeys();
		if (!ids.isEmpty()){
			
			ArrayList<String> allColumnNames = this.allColumnNames();
			
			sql+="INSERT INTO " + tableName;
			sql+="(";
			sql+= this.sepValuesByComma(allColumnNames);
			sql+=")";
			
			sql+="VALUES";
			
			boolean first = true;
			for (Integer id : ids){
				if (!first){
					sql+=",";
				}else{
					first = false;
				}
				sql+=this.rowValuesString(id,allColumnNames);
			}
			sql+=";";
			return sql;
		}else{
			throw new SQLObjException("No rows found in table " + tableName);
		}
	}

	public void setSchemaWritten(boolean b) {
		this.schemaWritten = b;
	}
	
	public boolean getSchemaWritten(){
		return this.schemaWritten;
	}
	
	public String getName(){
		return this.tableName;
	}

	public ArrayList<String> getDependencyTableNames() {
		ArrayList<String> dependencies = new ArrayList<String>();
		
		for (SQLValColumn col : this.columns.values()){
			if (col instanceof SQLForeignKeyColumn){
				SQLForeignKeyColumn forCol = (SQLForeignKeyColumn) col;
				String colDependency = forCol.getColumnReference().getTableName();
				dependencies.add(colDependency);
			}
		}
		return dependencies;
	}
	public Integer nextRowId() {
		return this.idColumn.getNextRowId();
	}
	
	public SQLColumnReference idColRef(){
		return new SQLColumnReference(this.getName(),this.idColumnName());
	}
}
