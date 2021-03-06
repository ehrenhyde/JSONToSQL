package solution;

import java.util.Map.Entry;
import java.util.TreeMap;

public class JSONObj extends JSONElement {
	private TreeMap<String, JSONSingleVal> valProps;
	private TreeMap<String, JSONObj> objProps;
	private TreeMap<String, JSONArray> arrayProps;

	public JSONObj(String json) throws JSONException {
		
		this.elementType = JSONElementType.OBJECT;

		this.valProps = new TreeMap<String, JSONSingleVal>();
		this.objProps = new TreeMap<String, JSONObj>();
		this.arrayProps = new TreeMap<String, JSONArray>();

		this.loadJSON(json);
	}

	public static int lenWholeObj(String tailString) throws JSONException {
		final int lenBrace = 1;
		int whiteLeft = tailString.indexOf("{");
		String remStrInsideObj = tailString.substring(whiteLeft+1);
		
		int objLen = 0;
		
		while(!JSONUtils.nextNonWhiteCharIs('}', remStrInsideObj)){
			
			int whiteLeftUntilElement = JSONUtils.lengthLeftWhiteSpaceAndCommaColon(remStrInsideObj);
			String upAgainstElement = remStrInsideObj.substring(whiteLeftUntilElement);
			int lenWholeNextElement = JSONElement.lenWholeElement(upAgainstElement);
			int elementAndWhiteLen = whiteLeftUntilElement+lenWholeNextElement;
			objLen += elementAndWhiteLen;
			
			remStrInsideObj = remStrInsideObj.substring(elementAndWhiteLen);

		}
		int lenWholeObj = whiteLeft + lenBrace*2 + objLen + JSONUtils.lengthLeftWhiteSpaceAndCommaColon(remStrInsideObj);//add space to and including the }
		return lenWholeObj;
	}

	@Override
	public String toString() {
		String result = "";

		result += "{";

		boolean firstProp = true;
		for (Entry<String, JSONSingleVal> map : this.valProps.entrySet()) {
			if (!firstProp) {
				result += ",";
			}
			result += "\"" + map.getKey() + "\"";
			result += ":";
			result +=map.getValue().toString();
			firstProp = false;
		}

		for (Entry<String, JSONObj> map : this.objProps.entrySet()) {
			if (!firstProp) {
				result += ",";
			}
			result += "\"" + map.getKey() + "\"";
			result += ":";
			result +=  map.getValue().toString();
		}

		for (Entry<String, JSONArray> map : this.arrayProps.entrySet()) {
			if (!firstProp) {
				result += ",";
			}
			result += "\"" + map.getKey() + "\"";
			result += ":";
			result += map.getValue().toString();
		}

		result += "}";

		return result;
	}

	public TreeMap<String, JSONSingleVal> getValProps() {
		return this.valProps;
	}

	public TreeMap<String, JSONObj> getObjProps() {
		return this.objProps;
	}

	public TreeMap<String, JSONArray> getArrayProps() {
		return this.arrayProps;
	}

	private void loadJSON(String json) throws JSONException {

		int cursor = json.indexOf("{") + 1;
		while (!JSONUtils.nextNonWhiteCharIs('}', json.substring(cursor))) {

			String tailString = json.substring(cursor);
			int lenConsumed = 0;

			//extract keyName
			String keyName = JSONSingleVal.wholeString(tailString);
			lenConsumed = JSONUtils.lenWholeString(tailString);
			cursor += lenConsumed;
			tailString = json.substring(cursor);

			//extract value
			if (JSONUtils.nextNonWhiteCharIs('"', tailString)) {
				JSONSingleVal singleVal = new JSONSingleVal(tailString);
				this.valProps.put(keyName, singleVal);
				lenConsumed = JSONSingleVal.lenWholeString(tailString);
			} else if (JSONUtils.nextNonWhiteCharIs('{', tailString)) {
				JSONObj jsonObj = new JSONObj(tailString);
				this.objProps.put(keyName, jsonObj);
				lenConsumed = JSONObj.lenWholeObj(tailString);
			} else if (JSONUtils.nextNonWhiteCharIs('[', tailString)) {
				JSONArray jsonArray = new JSONArray(tailString);
				this.arrayProps.put(keyName, jsonArray);
				lenConsumed = JSONArray.lenWholeArray(tailString);

			} else {
				throw new JSONException("Unexpected firstChar in JSONObj constructor loop");

			}
			cursor += lenConsumed;
		}
	}

	@Override
	public boolean equals(JSONElement jsonElement) throws JSONException {
		if (jsonElement.getType() == JSONElementType.OBJECT){
			
			JSONObj otherObj = (JSONObj)jsonElement;
			return this.toString().equals(otherObj.toString());
		}else{
			throw new JSONException("Comparing JSONElements of different type");
		}
	}

}
