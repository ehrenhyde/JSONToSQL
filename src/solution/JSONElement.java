package solution;

public class JSONElement {
	
	private String elementJSON;
	
	private JSONElementType elementType;
	
	public JSONElement(String json) throws JSONException{
		
		char firstChar = json.charAt(0);
		if (firstChar == '"'){
			elementType = JSONElementType.SINGLE_TYPE;
			this.elementJSON = JSONUtils.wholeString(json);
		}else if (firstChar == '{'){
			elementType = JSONElementType.OBJECT_TYPE;
			this.elementJSON = new JSONObj(json).toString();
		}else if(firstChar == '['){
			elementType = JSONElementType.ARRAY_TYPE;
			elementJSON = new JSONArray(json).toString();
		}
	}
	
	public static int lenWholeElement(String json) throws JSONException{
		char firstChar = json.charAt(0);
		if (firstChar == '"'){
			return JSONUtils.lenWholeString(json);
		}else if (firstChar == '{'){
			return JSONObj.lenWholeObj(json);
		}else if (firstChar == '['){
			return JSONArray.lenWholeArray(json);
		}else{
			throw new JSONException("Unexpected firstChar " + firstChar + "in lenWholeElement");
		}
	}
	
	public JSONElementType getType(){
		return this.elementType;
	}
	
	public String getElementJSON(){
		return this.elementJSON;
	}
	
}
