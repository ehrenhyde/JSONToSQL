package solution;

import java.util.ArrayList;

public class SQLIdColumn {
	ArrayList<String> keys;
	
	private String name;
	
	public SQLIdColumn(String name){
		this.name = name;
	}
	
	public void addKey(String key){
		this.keys.add(key);
	}
	
	public String getName(){
		return this.name;
	}
}
