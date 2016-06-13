package solution;

public class SQLForeignKeyColumn extends SQLValueColumn {
	private SQLColumnReference columnReference;
	
	public SQLForeignKeyColumn(SQLColumnReference referenceColumn){
		this.columnReference = referenceColumn;
	}
	
	public SQLColumnReference getColumnReference(){
		return this.columnReference;
	}
}
