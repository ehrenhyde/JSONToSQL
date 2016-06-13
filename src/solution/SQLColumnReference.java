package solution;

public class SQLColumnReference {
	private String tableName;
	private String columnName;
	
	public SQLColumnReference(String tableName, String columnName){
		this.tableName = tableName;
		this.columnName = columnName;
	}
	
	public String getTableName(){
		return tableName;
	}
	
	public String getColumnName(){
		return columnName;
	}
}
