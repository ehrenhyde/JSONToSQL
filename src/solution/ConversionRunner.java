package solution;

import java.io.IOException;
import java.sql.SQLException;

public class ConversionRunner {
	
	

	public static void main(String[] args) throws IOException {
		System.out.println("Starting");
		
		String fileName = TestJSONFileNames.duplicate_array_property_name;
		
		JSONFile file = new JSONFile(fileName);
		
		String json = file.readString();
		
		JSONObj jsonObj;
		
		try {
			jsonObj = new JSONObj(json);
			
			System.out.println(jsonObj.toString());
			
			SQLDatabase db = new SQLDatabase("Sam",jsonObj);
			
			db.writeAll();
			
			db.terminate();
			
		} catch (JSONException | SQLException | SQLObjException e) {
			e.printStackTrace();
		}	
	}

}
