package solution;

import java.util.ArrayList;
import java.util.Iterator;

public class JSONArray implements Iterable<JSONElement> {

	private ArrayList<JSONElement> elements;

	public JSONArray(String json) throws JSONException {

		this.elements = new ArrayList<JSONElement>();

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
			string+=element.getElementJSON();
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

	public boolean contains(JSONElement target) {
		boolean found = false;
		for (JSONElement ownElement : elements){
			if (ownElement.equals(target)){
				found = true;
				break;
			}
		}
		return (found);
	}

	public void remove(JSONElement target) {
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

	private void loadJSON(String json) throws JSONException {
		int indexOfBracket = json.indexOf('[');
		int cursor =  indexOfBracket + 1;
		while (!JSONUtils.nextNonWhiteCharIs(']', json.substring(cursor))){
			String restString = json.substring(cursor);
			JSONElement element = new JSONElement(restString);
			this.elements.add(element);

			int stringConsumed = JSONElement.lenWholeElement(restString);
			cursor += stringConsumed;
		}
	}

}
