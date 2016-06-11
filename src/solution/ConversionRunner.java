package solution;

import java.io.IOException;

public class ConversionRunner {
	
	

	public static void main(String[] args) throws IOException {
		System.out.println("Starting");
		
		String aFileName = TestJSONFileNames.THREE_TYPES;
		
		JSONFile aFile = new JSONFile(aFileName);
		
		String aJSON = aFile.readString();
		
		JSONObj aJSONObj;
		
		try {
			aJSONObj = new JSONObj(aJSON);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

}
