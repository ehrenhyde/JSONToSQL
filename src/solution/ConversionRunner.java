package solution;

import java.io.IOException;
import java.sql.SQLException;

public class ConversionRunner {
	
	

	public static void main(String[] args) throws IOException {
		System.out.println("Starting");
		
		String fileName = TestJSONFileNames.UNI_LARGE;
		
		JSONFile file = new JSONFile(fileName);
		
		String json = file.readString();
		
		
		
		try {
			JSONElementType type = JSONElement.getType(json);
			
			SQLDatabase db;
			String dbName = "done";
			
			if (type==JSONElementType.OBJECT){
				JSONObj jsonObj = new JSONObj(json);
				System.out.println(jsonObj.toString());
				
				db = new SQLDatabase(dbName,jsonObj);
				
				db.writeAll();
				
				db.terminate();
			}else if (type == JSONElementType.ARRAY){
				JSONArray jsonArray = new JSONArray(json);
				System.out.println(jsonArray.toString());
				db = new SQLDatabase(dbName,jsonArray);
				
				db.writeAll();
				
				db.terminate();
			}else if (type == JSONElementType.SINGLE_VAL){
				JSONSingleVal jsonSingleVal = new JSONSingleVal(json);
				System.out.println(jsonSingleVal.toString());
				db = new SQLDatabase(dbName,jsonSingleVal);
				
				db.writeAll();
				
				db.terminate();
			}
		
			
			
		} catch (JSONException | SQLException | SQLObjException e) {
			e.printStackTrace();
		}	
		
		System.out.println("done");
	}

}
