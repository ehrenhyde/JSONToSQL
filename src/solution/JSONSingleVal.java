package solution;

public class JSONSingleVal extends JSONElement {

	private String val;
	
	public JSONSingleVal(String json) throws JSONException {
		this.val = JSONSingleVal.wholeString(json);
		this.elementType=JSONElementType.SINGLE_VAL;
	}

	@Override
	public String toString() {
		return "\"" + val + "\"";
	}
	
	public static String wholeString(String tailString) throws JSONException {
		return JSONUtils.wholeString(tailString);
	}
	
public static int lenWholeString(String tailString) throws JSONException {
		
		return JSONUtils.lenWholeString(tailString);
	}

@Override
public boolean equals(JSONElement jsonElement) throws JSONException {
	if (jsonElement.getType() == JSONElementType.SINGLE_VAL){
		String thisVal = this.val;
		String otherVal = ((JSONSingleVal) jsonElement).val;
		return thisVal.equals(otherVal);
	}else{
		throw new JSONException("Comparing JSONElements of different type");
	}
}

}
