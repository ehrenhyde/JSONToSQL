package solution;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class JSONFile {
	
	public static final String dir = "C:\\Users\\Ehren\\OneDrive\\Documents\\Personal Projects\\Java Projects\\JSONToSQL\\json\\";
	private static final Charset ENCODING = StandardCharsets.UTF_8;
	
	
	private String filePath;
	
	
	public JSONFile(String filePath){
		
		this.filePath = filePath;
		
	}
	
	 public List<String> readLines() throws IOException {
	    Path path = Paths.get(JSONFile.dir+this.filePath);
	    return Files.readAllLines(path, ENCODING);
	  }
	 
	 public String readString() throws IOException{
		 List<String> lines = readLines();
		 String combined = "";
		 for (String line : lines){
			 combined += line;
		 }
		 String trimmed = JSONUtils.trimLeftWhitespaceAndCommaColon(combined);
		 return trimmed;
	 }

}
