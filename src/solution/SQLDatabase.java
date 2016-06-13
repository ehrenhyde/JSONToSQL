package solution;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.TreeMap;

public class SQLDatabase {
	
	private final String pathToPassword = "C:\\Users\\Ehren\\OneDrive\\Documents\\Personal Projects\\Java Projects\\JSONToSQL\\resources\\secret\\password.txt";
	private String dbName;
	TreeMap<String,SQLTable> tables;
	
	private Connection con;
	
	public SQLDatabase(String dbName) throws SQLException, IOException{
		this.dbName = dbName;
		this.tables = new TreeMap<String,SQLTable>();
		
		// This will load the MySQL driver, each DB has its own driver
	      //Class.forName("com.mysql.jdbc.Driver");
	      // Setup the connection with the DB
	      this.con = this.getConnection();
	      
	}
	
	public void addTable(String tableName,SQLTable table){
		this.tables.put(tableName, table);
	}
	
	private String getPassword() throws IOException{
		Path path = Paths.get(this.pathToPassword);
		String password = Files.readAllLines(path,StandardCharsets.UTF_8).get(0);
		return password;
	}
	
	private Connection getConnection() throws SQLException, IOException{
		String password = this.getPassword();
		return DriverManager.getConnection("jdbc:mysql://localhost/jsonTest?useSSL=false"
			              + "&user=jsonToSQL&password="+password);
	}
	
	private SQLTable getTable(String tableName) throws SQLObjException{
		if (this.tables.containsKey(tableName)){
			return this.tables.get(tableName);
		}else{
			throw new SQLObjException("Trying to get table which doesn't exist in this database");
		}
	}

	public ResultSet executeSQL(String sql) throws SQLException {
		// Statements allow to issue SQL queries to the database
	      Statement stmt = con.createStatement();
	      // Result set get the result of the SQL query
	      ResultSet resultSet = stmt
	          .executeQuery(sql);
	      return resultSet;
	}
}
