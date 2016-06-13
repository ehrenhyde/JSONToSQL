package solution;

import java.util.TreeMap;

public abstract class SQLValueColumn {
	private TreeMap<String,String> cells;
	
	public SQLValueColumn(){
		this.cells = new TreeMap<String,String>();
	}
	
	public void addCell(String rowKey, String value){
		this.cells.put(rowKey, value);
	}
	
}
