package solution;

import java.io.IOException;
import java.util.ArrayList;

public class ConversionRunner {
	
	

	public static void main(String[] args) throws IOException {
		System.out.println("Starting");
		
		String aFileName = TestJSONFileNames.UNI_LARGE;
		
		JSONFile aFile = new JSONFile(aFileName);
		
		String aJSON = aFile.readString();
		
		JSONObj aJSONObj;
		
		try {
			aJSONObj = new JSONObj(aJSON);
			
			System.out.println(aJSONObj.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}	
	}

}
