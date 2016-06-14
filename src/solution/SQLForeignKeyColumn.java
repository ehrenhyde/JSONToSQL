package solution;

public class SQLForeignKeyColumn extends SQLValColumn {
	private SQLColumnReference columnReference;
	
	public SQLForeignKeyColumn(String name,SQLColumnReference referenceColumn){
		super(name);
		this.columnReference = referenceColumn;
	}
	
	public SQLColumnReference getColumnReference(){
		return this.columnReference;
	}
}
