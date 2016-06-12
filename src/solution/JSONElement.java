package solution;

public class JSONElement {
	
	private String elementJSON;
	
	private JSONElementType elementType;
	
	public JSONElement(String tailString) throws JSONException{
		
		int whiteLeft = JSONUtils.lengthLeftWhiteSpaceAndCommaColon(tailString);
		String fromElementStart = tailString.substring(whiteLeft);
		
		char firstChar = fromElementStart.charAt(0);
		if (firstChar == '"'){
			elementType = JSONElementType.SINGLE_TYPE;
			this.elementJSON = JSONUtils.wholeString(fromElementStart);
		}else if (firstChar == '{'){
			elementType = JSONElementType.OBJECT_TYPE;
			this.elementJSON = new JSONObj(fromElementStart).toString();
		}else if(firstChar == '['){
			elementType = JSONElementType.ARRAY_TYPE;
			elementJSON = new JSONArray(fromElementStart).toString();
		}else{
			throw new JSONException("Unexpected element start *"+firstChar+"*");
		}
		
	
	}
	
	public static int lenWholeElement(String json) throws JSONException{
		
		final int lenOpen = 1;
		final int lenClose = 1;
		
		int indexParentStart = JSONUtils.indexOfPropertyStartChars(json);
		char elementParentOpening = json.charAt(indexParentStart);
		
		if (elementParentOpening == '"'){
			return JSONUtils.lenWholeString(json);
		}else{
			String remStrInsideParent = json.substring(indexParentStart+1);
			
			int parentLen = 0;
			
			while(!JSONUtils.nextNonWhiteCharCloses(remStrInsideParent, elementParentOpening)){
				
				int whiteLeftUntilSubElement = JSONUtils.lengthLeftWhiteSpaceAndCommaColon(remStrInsideParent);
				String upAgainstSubElement = remStrInsideParent.substring(whiteLeftUntilSubElement);
				int lenWholeNextSubElement = JSONElement.lenWholeElement(upAgainstSubElement);
				int subElementAndWhiteLen = whiteLeftUntilSubElement+lenWholeNextSubElement;
				parentLen += subElementAndWhiteLen;
				
				remStrInsideParent = remStrInsideParent.substring(subElementAndWhiteLen);
	
			}
			int lenWholeObj = indexParentStart + lenOpen + parentLen + JSONUtils.lengthLeftWhiteSpaceAndCommaColon(remStrInsideParent) + lenClose;//add space to and including the }
			return lenWholeObj;
		}
		
		
		/*if (JSONUtils.nextNonWhiteCharIs('"', json)){
			return JSONUtils.lenWholeString(json);
		}else if(JSONUtils.nextNonWhiteCharIs('{', json)){
			return JSONObj.lenWholeObj(json);
		}else if (JSONUtils.nextNonWhiteCharIs('[', json)){
			return JSONArray.lenWholeArray(json);
		}else{
			throw new JSONException("Unexpected next non white char in lenWholeElement");
		}*/
	}
	
	public JSONElementType getType(){
		return this.elementType;
	}
	
	public String getElementJSON(){
		return this.elementJSON;
	}
	
	public boolean equals(JSONElement s){
		String sJSON = s.elementJSON;
		String eJSON = this.elementJSON;
		
		JSONElementType sType = s.elementType;
		JSONElementType eType = this.elementType;
		
		return (sJSON.equals(eJSON) && sType.equals(eType));
	}
	
}
