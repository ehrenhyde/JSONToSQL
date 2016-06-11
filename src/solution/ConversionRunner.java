package solution;

import java.io.IOException;

public class ConversionRunner {
	
	private static final String dir = "C:\\Users\\Ehren\\OneDrive\\Documents\\Personal Projects\\Java Projects\\JSONToSQL\\json\\";
	

	public static void main(String[] args) throws IOException {
		System.out.println("Starting");
		
		String aFileName = TestJSONFileNames.THREE_TYPES;
		
		JSONFile aFile = new JSONFile(dir+aFileName);
		
		String aJSON = aFile.readString();
		
		JSONObj aJSONObj;
		
		try {
			aJSONObj = new JSONObj(aJSON);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

}
