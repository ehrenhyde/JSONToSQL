package solution;

public abstract class JSONElement {
	
	public JSONElementType elementType;
	
	public JSONElement(){
		this.elementType = null;
	}
	
	public abstract boolean equals(JSONElement jsonElement) throws JSONException;
	
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
	}
	
	public abstract String toString();
	
	public JSONElementType getType(){
		return this.elementType;
	}
	
	public static JSONElementType getType(String json) throws JSONException{
		if (JSONUtils.nextNonWhiteCharIs('"', json)){
			return JSONElementType.SINGLE_VAL;
		}else if (JSONUtils.nextNonWhiteCharIs('{', json)){
			return JSONElementType.OBJECT;
		}else if (JSONUtils.nextNonWhiteCharIs('[', json)){
			return JSONElementType.ARRAY;
		}else{
			throw new JSONException("Unexpected opening character when determining type");
		}
	}
	
}
