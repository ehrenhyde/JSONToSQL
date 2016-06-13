package solution;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

public class SQLTable {
	private SQLIdColumn idColumn;
	private TreeMap<String, SQLValueColumn> columns;

	public SQLTable(SQLIdColumn idCol) {
		this.columns = new TreeMap<String, SQLValueColumn>();
		this.setIdColumn(idCol);
	}

	private void setIdColumn(SQLIdColumn idCol) {
		this.idColumn = idCol;
	}

	public void addRow(String rowId, TreeMap<String, String> otherVals) throws SQLObjException {
		this.addToIdColumn(rowId);
		for (Entry<String, String> keyValPair : otherVals.entrySet()) {
			String columnName = keyValPair.getKey();
			String cellVal = keyValPair.getValue();

			if (!this.columns.containsKey(columnName)) {
				throw new SQLObjException("Trying to add row value for which no column exists");
			}
			SQLValueColumn column = this.columns.get(columnName);
			column.addCell(rowId, cellVal);
			this.columns.put(columnName, column);
		}
	}

	public void addForeignKeyColumn(String columnName, SQLColumnReference referenceColumnName) {
		SQLForeignKeyColumn column = new SQLForeignKeyColumn(referenceColumnName);
		this.columns.put(columnName, column);
	}

	public void addValueColumn(String columnName) {
		this.columns.put(columnName, new SQLSimpleValColumn());
	}

	private void addToIdColumn(String rowId) {
		this.idColumn.addKey(rowId);
	}

	public TreeMap<String, SQLSimpleValColumn> getSimpleValCols() {
		TreeMap<String, SQLSimpleValColumn> simpleValCols = new TreeMap<String, SQLSimpleValColumn>();
		for (Entry<String, SQLValueColumn> nameAndCol : this.columns.entrySet()) {
			if (nameAndCol.getValue() instanceof SQLSimpleValColumn) {
				simpleValCols.put(nameAndCol.getKey(), (SQLSimpleValColumn) nameAndCol.getValue());
			}
		}
		return simpleValCols;
	}

	public TreeMap<String, SQLForeignKeyColumn> getForeignKeyCols() {
		TreeMap<String, SQLForeignKeyColumn> foreignKeyCols = new TreeMap<String, SQLForeignKeyColumn>();
		for (Entry<String, SQLValueColumn> nameAndCol : this.columns.entrySet()) {
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
		sql += "`" + keyColName + "` VARCHAR(1024) NOT NULL,";
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
		sql += "`" + colName + "` VARCHAR(1024) NOT NULL, ";
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
		boolean first = true;
		for (Entry<String, SQLValueColumn> keyVal : this.columns.entrySet()) {

			sql += ",";

			String columnName = keyVal.getKey();
			SQLValueColumn col = keyVal.getValue();
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
}
