package solution;

import java.util.TreeMap;

public abstract class SQLValColumn {
	private String name;
	private TreeMap<Integer,String> cells;
	
	public SQLValColumn(String name){
		this.name = name;
		this.cells = new TreeMap<Integer,String>();
	}
	
	public void addCell(Integer newId, JSONSingleVal value){
		String valueString = value.getVal();
		this.cells.put(newId, valueString);
	}
	
	public String getValForId(Integer id){
		return this.cells.get(id);
	}
	
	public boolean containsValForId(Integer id){
		return this.cells.containsKey(id);
	}
	
	public String getName(){
		return this.name;
	}

	public boolean equalsCol(SQLValColumn otherCol) {
		String thisName = this.name;
		String otherName = otherCol.name;
		return(thisName.equals(otherName));
	}
}
