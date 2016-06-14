package solution;

import java.util.ArrayList;

public class SQLIdColumn{
	private ArrayList<Integer> keys;
	
	private final String name = "id";
	
	public SQLIdColumn(){
		this.keys = new ArrayList<Integer>();
	}
	
	public Integer addKey(){
		int nextId = keys.size()+1;
		this.keys.add(nextId);
		return nextId;
	}
	
	public String getName(){
		return this.name;
	}
	
	public ArrayList<String> getKeyStrings(){
		ArrayList<String> ids = new ArrayList<String>();
		for (int key : this.keys){
			ids.add(key + "");
		}
		return ids;
	}
	
	public ArrayList<Integer> getKeys(){
		return this.keys;
	}

	public Integer getNextRowId() {
		int nextId = keys.size()+1;
		return nextId;
	}
}
