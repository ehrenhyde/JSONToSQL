package solution;

import java.util.ArrayList;
import java.util.Iterator;

public class JSONArray extends JSONElement implements Iterable<JSONElement> {

	private ArrayList<JSONElement> elements;
	private JSONElementType subElementType;

	public JSONArray(String json) throws JSONException {
		super();
		this.elements = new ArrayList<JSONElement>();
		this.elementType = JSONElementType.ARRAY;
		this.subElementType = null;

		this.loadJSON(json);
	}
	
	public JSONArray(ArrayList<JSONElement> elements) throws JSONException {

		this.elements = elements;
	}

	@Override
	public String toString() {
		String string = "";
		string+="[";
		boolean first = true;
		for (JSONElement element : this.elements){
			if (!first){
				string+=",";
			}
			string+=element.toString();
			first = false;
		}
		string+="]";
		return string;
	}

	public static int lenWholeArray(String json) throws JSONException {
		return JSONElement.lenWholeElement(json);
	}

	@Override
	public Iterator<JSONElement> iterator() {
		return this.elements.iterator();
	}

	public boolean contains(JSONElement target) throws JSONException {
		boolean found = false;
		for (JSONElement ownElement : elements){
			if (ownElement.equals(target)){
				found = true;
				break;
			}
		}
		return (found);
	}

	public void remove(JSONElement target) throws JSONException {
		ArrayList<JSONElement> toRemove = new ArrayList<JSONElement>();
		for (JSONElement ownElement : this.elements){
			if (ownElement.equals(target)){
				toRemove.add(ownElement);
			}
		}
		for (JSONElement itemToRemove : toRemove){
			this.elements.remove(itemToRemove);
		}
	}

	public JSONElement get(int i) {
		return this.elements.get(0);
	}

	public boolean isEmpty() {
		return this.elements.isEmpty();
	}

	public ArrayList<JSONElement> getElements() {
		return this.elements;
	}
	
	public void add(JSONElement element) throws JSONException{
		if (this.subElementType != null){
			if (element.getType() == this.subElementType){
				this.elements.add(element);
			}else{
				throw new JSONException("Trying to add different JSONElementType to JSONArray");
			}
		}else{
			this.elements.add(element);
			this.subElementType = element.getType();
		}
		;
	}
	
	public void add(String jsonElement) throws JSONException{
		JSONElement element;
		JSONElementType type = JSONElement.getType(jsonElement);
		
		
		switch(type){
		case SINGLE_VAL:
			element = new JSONSingleVal(jsonElement);
			break;
		case OBJECT:
			element = new JSONObj(jsonElement);
			break;
		case ARRAY:
			element = new JSONArray(jsonElement);
			break;
		default:
			throw new JSONException("Unexpected type when adding to JSONArray");
		}
		
		this.add(element);
	}

	private void loadJSON(String json) throws JSONException {
		int indexOfBracket = json.indexOf('[');
		int cursor =  indexOfBracket + 1;
		while (!JSONUtils.nextNonWhiteCharIs(']', json.substring(cursor))){
			String restString = json.substring(cursor);
			
			this.add(restString);
			
			int stringConsumed = JSONElement.lenWholeElement(restString);
			cursor += stringConsumed;
		}
	}
	
	public JSONArray copy() throws JSONException{
		JSONArray copy = new JSONArray(this.elements);
		return copy;
	}

	@Override
	public boolean equals(JSONElement jsonElement) throws JSONException {
		JSONElementType targetType = jsonElement.elementType;
		if (targetType==this.elementType){
			JSONArray targetArray = (JSONArray) jsonElement;
			JSONArray copyThis = this.copy();
			
			for (JSONElement targetElement : targetArray){
				if (copyThis.contains(targetElement)){
					copyThis.remove(targetElement);
				}else{
					return false;
				}
			}
			
			if (!copyThis.isEmpty()){
				return false;
			}
			return true;
		}else{
			throw new JSONException("Comparing objects of different type");
		}
		
		
	}

}
