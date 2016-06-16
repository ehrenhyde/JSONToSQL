package solution;

public class SQLColumnReference implements Comparable<SQLColumnReference>{
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
	
	@Override
	public String toString(){
		return this.getTableName() + "_" + this.getColumnName();
	}

	@Override
	public int compareTo(SQLColumnReference otherColRef) {
		String otherColRefString = otherColRef.toString();
		String thisColRefString = this.toString();
		if (otherColRefString.equals(thisColRefString)){
			return 0;
		}else{
			return -1;
		}
	}
}
