package solution;

import java.util.ArrayList;

public class JSONArray {
	
	private ArrayList<JSONElement> elements;

	public JSONArray(String json) throws JSONException {
		int indexOfBracket = json.indexOf('[');
		for (int i = indexOfBracket+1;json.charAt(i)!=']';){
			String restString = json.substring(i);
			JSONElement element = new JSONElement(restString);
			elements.add(element);
			
			int stringConsumed = JSONElement.lenWholeElement(restString);
			i+=stringConsumed;
		}
	}
	
	@Override
	public String toString(){
		//ToDo
		return null;
	}

	public static int lenWholeArray(String json) throws JSONException {
		int i;
		int indexOfBracket = json.indexOf('[');
		for (i=indexOfBracket;json.charAt(i)!=']';){
			String restString = json.substring(i);
			
				i+=JSONElement.lenWholeElement(restString);
		}
		int lenWholeArray = i + 1;//include ]
		return lenWholeArray;
	}

}
