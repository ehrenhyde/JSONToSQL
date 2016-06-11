package solution;

import java.util.Map.Entry;
import java.util.TreeMap;

public class JSONObj {
	private TreeMap<String,String> valProps;
	private TreeMap<String, JSONObj> objProps;
	private TreeMap<String,JSONArray> arrayProps;
	
	public JSONObj(String json) throws JSONException{
		
		valProps = new TreeMap<String,String>();
		objProps = new TreeMap<String, JSONObj>();
		arrayProps = new TreeMap<String,JSONArray>();
		
		this.loadJSON(json);
	}
	
	public static int lenWholeObj(String tailString) throws JSONException{
		int whiteLeft = tailString.indexOf("{");
		int i;
		for (i = whiteLeft;tailString.charAt(i)!='}';){
			String restString = tailString.substring(i);
			int lenWholeUnknown = JSONElement.lenWholeElement(restString);
			i+=lenWholeUnknown;
			
			
		}
		int lenWholeObj = i+1;//include the }
		return lenWholeObj;
	}
	
	@Override
	public String toString(){
		String result = "";
		
		result += "{";
		
		boolean firstProp = true;
		for ( Entry<String, String> map : this.valProps.entrySet()){
			if (!firstProp){
				result+=",";
			}
			result+="\""+map.getKey() + "\"";
			result+=":";
			result+="\""+map.getValue()+ "\"";
			firstProp = false;
		}
		
		for (Entry<String,JSONObj> map: this.objProps.entrySet()){
			if (!firstProp){
				result+=",";
			}
			result+="\""+map.getKey() + "\"";
			result+=":";
			result+="\""+map.getValue().toString()+ "\"";
		}
		
		for (Entry<String, JSONArray> map: this.arrayProps.entrySet()){
			if (!firstProp){
				result+=",";
			}
			result+="\""+map.getKey() + "\"";
			result+=":";
			result+="\""+map.getValue().toString()+ "\"";
		}
		
		result+="}";
		
		return result;
	}
	
	public TreeMap<String,String> getValProps(){
		return this.valProps;
	}
	
	public TreeMap<String,JSONObj> getObjProps(){
		return this.objProps;
	}
	
	public TreeMap<String,JSONArray> getArrayProps(){
		return this.arrayProps;
	}
	
	private void loadJSON(String json) throws JSONException{
		boolean readKey = true;
		String keyName = "";
		
		for (int i = json.indexOf("{")+1;json.charAt(i)!='}';){
			String tailString = json.substring(i,json.length());
			if (readKey){
				
				keyName = JSONUtils.wholeString(tailString);
				int lenConsumed = JSONUtils.lenWholeString(tailString);
				i+=lenConsumed;
			}else{
				
				String fromProperty = JSONUtils.trimLeftWhitespaceAndCommaColon(tailString);
				
				char firstChar = fromProperty.charAt(0);
				if (firstChar == '"'){
					String val = JSONUtils.wholeString(fromProperty);
					valProps.put(keyName, val);
					int lenConsumed = JSONUtils.lenWholeString(tailString);
					i+=lenConsumed;
				}else if(firstChar == '{'){
					JSONObj jsonObj = new JSONObj(fromProperty);
					objProps.put(keyName, jsonObj);
					int lenConsumed = JSONObj.lenWholeObj(tailString);
					i+=lenConsumed;
				}else if(firstChar == '['){
					JSONArray jsonArray = new JSONArray(fromProperty);
					arrayProps.put(keyName, jsonArray);
					
					int lenConsumed = JSONArray.lenWholeArray(tailString);
					i+=lenConsumed;
					
				}else{
					throw new JSONException("Unexpected firstChar in JSONObj constructor loop");
					
				}
			}
			readKey=!readKey;
		}
	}
	
}
